package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.zip.*;

/** Isolated Skill versions and Vault secrets. Never starts a Session or contacts an MCP server. */
public final class SkillVaultExample {
  private SkillVaultExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), System.out);
    }
  }

  public static void run(ForwardClient client, PrintStream out) {
    String batch = UUID.randomUUID().toString(), name = "sdk-java-" + batch;
    String skillId = null, vaultId = null, credentialId = null;
    RuntimeException failure = null;
    out.println(
        "example_batch="
            + batch
            + " skill_create_key="
            + batch
            + "-skill vault_create_key="
            + batch
            + "-vault credential_create_key="
            + batch
            + "-credential");
    try {
      Skill skill =
          client
              .skills()
              .create(
                  SkillCreateParams.builder()
                      .addFile(packageUpload(name, "Initial example"))
                      .build(),
                  writeOptions(batch + "-skill"));
      skillId = skill.id();
      out.println("created_skill=" + skillId);
      client.skills().retrieve(skillId, readOptions());
      client
          .skills()
          .list(SkillListParams.builder().displayTitle(name).limit(10).build(), readOptions());
      client.skills().versions().list(skillId, readOptions());
      SkillVersion version =
          client
              .skills()
              .versions()
              .create(
                  skillId,
                  SkillVersionCreateParams.builder()
                      .addFile(packageUpload(name, "Updated example"))
                      .build(),
                  readOptions().toBuilder().maxRetries(0).build());
      String versionId = version.version();
      out.println("created_skill_version=" + versionId);
      client.skills().versions().retrieve(skillId, versionId, readOptions());
      try (FileDownload download =
          client.skills().versions().download(skillId, versionId, readOptions())) {
        // Downloads wrap the canonical ZIP in a top-level directory, changing its byte hash.
        long size = verifyPackage(download.body(), name, skillDocument(name, "Updated example"));
        out.println("verified_skill_bytes=" + size + " request_id=" + download.requestId());
      } catch (IOException e) {
        throw new IllegalStateException("Unable to verify Skill download", e);
      }
      client
          .skills()
          .versions()
          .delete(skillId, versionId, writeOptions(batch + "-version-delete"));
      Vault vault =
          client
              .vaults()
              .create(
                  VaultCreateParams.builder().displayName(name).build(),
                  writeOptions(batch + "-vault"));
      vaultId = vault.id();
      out.println("created_vault=" + vaultId);
      client.vaults().retrieve(vaultId, readOptions());
      client.vaults().list(VaultListParams.builder().name(name).limit(10).build(), readOptions());
      VaultCredential credential =
          client
              .vaults()
              .credentials()
              .create(
                  vaultId,
                  VaultCredentialCreateParams.builder()
                      .auth(
                          VaultCredentialCreateAuth.builder()
                              .type("environment_variable")
                              .secretName("SDK_JAVA_EXAMPLE")
                              .secretValue(UUID.randomUUID().toString())
                              .build())
                      .build(),
                  writeOptions(batch + "-credential"));
      credentialId = credential.id();
      out.println("created_credential=" + credentialId);
      client.vaults().credentials().retrieve(vaultId, credentialId, readOptions());
      client.vaults().credentials().list(vaultId, readOptions());
      VaultCredential rotated =
          client
              .vaults()
              .credentials()
              .update(
                  vaultId,
                  credentialId,
                  VaultCredentialUpdateParams.builder()
                      .auth(
                          VaultCredentialUpdateAuth.builder()
                              .type("environment_variable")
                              .secretValue(UUID.randomUUID().toString())
                              .build())
                      .metadata(
                          Collections.singletonMap("source", JsonValue.of("sdk-java-example")))
                      .build(),
                  readOptions());
      if (rotated.auth() == null || !"SDK_JAVA_EXAMPLE".equals(rotated.auth().secretName()))
        throw new IllegalStateException("Unexpected credential response");
      // A redacted GET cannot prove the stored secret. Success is the update acknowledgement.
      out.println("credential_rotation_acknowledged request_id=" + rotated.requestId());
    } catch (RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      RuntimeException cleanup = null;
      if (credentialId != null)
        try {
          client
              .vaults()
              .credentials()
              .delete(vaultId, credentialId, writeOptions(batch + "-credential-delete"));
          out.println("deleted_credential=" + credentialId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_credential=" + credentialId);
          cleanup = e;
        }
      if (vaultId != null)
        try {
          client.vaults().delete(vaultId, writeOptions(batch + "-vault-delete"));
          out.println("deleted_vault=" + vaultId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_vault=" + vaultId);
          if (cleanup == null) cleanup = e;
          else cleanup.addSuppressed(e);
        }
      if (skillId != null)
        try {
          client.skills().delete(skillId, writeOptions(batch + "-skill-delete"));
          out.println("deleted_skill=" + skillId);
        } catch (RuntimeException e) {
          out.println("cleanup_failed_skill=" + skillId);
          if (cleanup == null) cleanup = e;
          else cleanup.addSuppressed(e);
        }
      if (cleanup != null) {
        if (failure != null) failure.addSuppressed(cleanup);
        else throw cleanup;
      }
    }
  }

  private static Upload packageUpload(String name, String description) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      zip.putNextEntry(new ZipEntry(name + "/SKILL.md"));
      zip.write(skillDocument(name, description));
      zip.closeEntry();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to build Skill package", e);
    }
    return Upload.bytes(bytes.toByteArray(), name + ".zip", "application/zip");
  }

  private static byte[] skillDocument(String name, String description) {
    return ("---\nname: "
            + name
            + "\ndescription: "
            + description
            + "\n---\nThis is an isolated SDK packaging example.\n")
        .getBytes(StandardCharsets.UTF_8);
  }

  static long verifyPackage(InputStream input, String name, byte[] expected) throws IOException {
    boolean found = false;
    long size = 0;
    int entries = 0;
    // Read in memory without extracting server-controlled paths to disk.
    try (ZipInputStream zip = new ZipInputStream(input)) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (++entries > 4) throw new IllegalStateException("Unexpected extra Skill ZIP entries");
        if (entry.isDirectory() && (name + "/").equals(entry.getName())) continue;
        if (found || !(name + "/SKILL.md").equals(entry.getName()))
          throw new IllegalStateException("Unexpected Skill ZIP member");
        int offset = 0, count;
        byte[] buffer = new byte[1024];
        while ((count = zip.read(buffer)) != -1) {
          size += count;
          for (int i = 0; i < count; i++)
            if (offset >= expected.length || buffer[i] != expected[offset++])
              throw new IllegalStateException("Downloaded SKILL.md content differs");
        }
        if (offset != expected.length)
          throw new IllegalStateException("Downloaded SKILL.md is incomplete");
        found = true;
      }
    }
    if (!found) throw new IllegalStateException("Downloaded package has no SKILL.md");
    return size;
  }

  private static RequestOptions readOptions() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }

  private static RequestOptions writeOptions(String key) {
    return readOptions().toBuilder().idempotencyKey(key).build();
  }
}
