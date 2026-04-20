GRADLEW := ./gradlew
GRADLE_JAVA_HOME ?= /usr/lib/jvm/java-17-openjdk-amd64

.PHONY: all build debug clean

all: build

build:
	$(GRADLEW) -Dorg.gradle.java.home=$(GRADLE_JAVA_HOME) assembleDebug

debug: build

clean:
	$(GRADLEW) -Dorg.gradle.java.home=$(GRADLE_JAVA_HOME) clean
	rm -rf build app/build .gradle