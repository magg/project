#!/bin/bash
#
# detectOS
#
# Detects which OS and if it is Linux then it will detect which Linux
# Distribution.
#

OS=`uname -s`
RESULT="unknown"

function detectOS(){
  if [ "${OS}" = "SunOS" ] ; then
    RESULT="Solaris"
  elif [ "${OS}" = "Darwin" ] ; then
    RESULT= "MACOS"
  elif [ "${OS}" = "Linux" ] ; then
    if [ -f '/etc/oracle-release' ] ; then
      RESULT="Oracle"
    elif [ -f '/etc/centos-release' ] ; then
      RESULT="Centos"
    elif [ -f '/etc/redhat-release' ] ; then
      if [ "$(cat /etc/redhat-release | grep -i 'Red Hat Enterprise')x" != "x" ]; then
        RESULT="rhel"
      else
        RESULT=$(cat /etc/redhat-release | cut -d ' ' -f1)
      fi
    elif [ -f '/etc/SuSE-release' ] ; then
      RESULT="SuSE"
    elif [ -f '/etc/mandrake-release' ] ; then
      RESULT="Mandrake"
    elif [ -f '/etc/debian_version' ] ; then
      if [ -r '/etc/dpkg/origins/ubuntu' ]; then
        RESULT="Ubuntu"
      else
        RESULT="Debian"
      fi
    elif [ -r '/etc/sles-release' ]; then
      RESULT="Sles"
    fi
  fi
}

detectOS
echo "$RESULT"
