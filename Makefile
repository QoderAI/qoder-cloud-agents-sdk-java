.PHONY: quality format test package-check

# Formatting uses JDK 17+; SDK compilation and runtime support still start at Java 8.
quality:
	python3 .github/scripts/format_java.py
	python3 .github/scripts/check_repository.py
	python3 -m unittest discover -s .github/scripts -p 'test_*.py'

format:
	python3 .github/scripts/format_java.py --apply

test:
	./mvnw -B -ntp clean install
	./mvnw -B -ntp -f examples/pom.xml clean verify
	python3 .github/scripts/check_package.py

package-check:
	python3 .github/scripts/check_package.py
