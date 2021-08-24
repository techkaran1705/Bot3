#!/bin/bash
# Made by Velosh <daffetyxd@gmail.com> for TrebleExperience/Bot3

# Core variables.
distro=$(awk -F= '$1 == "ID" {print $2}' /etc/os-release)
APATH=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# Util function
SETUP() {
    if $(sudo update-alternatives --install "/usr/bin/java" "java" "/usr/java/oracle/jdk-16.0.1/bin/java" 1 >> /dev/null 2>&1); then
        echo "-> Successfully installed Java symlink."
    else
        echo "-> Fail to install Java (16.0.1) symlink."
    fi
    if $(sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/java/oracle/jdk-16.0.1/bin/javac" 1 >> /dev/null 2>&1); then
        echo "-> Successfully installed Javac symlink."
    else
        echo "-> Fail to install Javac (16.0.1) symlink."
    fi
}

# Warn
echo "-> Make sure you've wget installed."

# Start the process.
if [ "${EUID}" -ne 0 ]; then
    echo "-> Run as root!"
    exit 1
fi

if [[ "$OSTYPE" == "linux-gnu" ]]; then
    if [[ ! "$distro" == "arch" ]]; then
        if $(sudo apt-get purge default-jdk default-jre-headless openjdk-*-jdk openjdk-*-jdk-headless openjdk-*jre-headless openjdk-*-jre >> /dev/null 2>&1); then
            echo "-> Successfully uninstalled! Attempting to install Java 16.0.1, wait."
            if [ -d "/usr/java/oracle/jdk-16.0.1" ]; then
                if [ -f "/usr/java/oracle/jdk-16.0.1/bin/java" ]; then
                    echo "-> It looks like it is already installed, so wait a moment..."
                    SETUP
                else
                    echo "-> Delete the folder: /usr/java/oracle/jdk-16.0.1 and try the process again."
                    exit 1
                fi
            else
                echo "-> Create Java 16.0.1 path dir"
                sudo mkdir -p /usr/java/oracle
                cd /usr/java/oracle
                echo "-> Downloading JDK 16.0.1..."
                sudo wget 'https://github.com/TrebleExperience/Bot3-Workflow/releases/download/J16/jdk-16.0.1_linux-x64_bin.tar.gz' -O linux.tar.gz >> /dev/null 2>&1
                echo "-> Extracting..."
                sudo tar -xzvf linux.tar.gz >> /dev/null 2>&1
                export JAVA_HOME=/usr/java/oracle/jdk-16.0.1
                export PATH=$PATH:$JAVA_HOME/bin
                echo "JAVA_HOME=/usr/java/oracle/jdk-16.0.1" >> /etc/profile
                echo "PATH=$PATH:$JAVA_HOME/bin" >> /etc/profile
                SETUP
                source /etc/profile
                echo "-> Done. Process ready."
                java -version
            fi
        else
            echo "-> Unknown error."
            exit 1
        fi
    fi
else
    echo "-> You need Ubuntu based distro to use this script, abort."
    exit 1
fi
