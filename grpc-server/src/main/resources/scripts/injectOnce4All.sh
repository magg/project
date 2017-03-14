#!/bin/bash
#
# injectOnce
#
# Injection utility used to introduce "Once4All" injections.
#
# Requires a Linux operating system.
# Systems tested: Debian, CentOS, Oracle, RHEL, Ubuntu, Sles and Rhel
#
# Author: Francesco Sorrentino <francesco@cloudera.com>
#

command=help

############

function f_suicide(){
  echo 'Shutting down the machine...'
  echo ' '
  sudo echo 1 > /proc/sys/kernel/sysrq;
  sudo echo o > /proc/sysrq-trigger;
}

function f_panic(){
  echo 'Injecting kernel panic...'
  echo ' '
  sudo echo 1 > /proc/sys/kernel/sysrq;
  echo c > /proc/sysrq-trigger;
}

while test $# -gt 0
do
  case $1 in
    -h | --help)
      # usage and help
      command=help
      break
      ;;
    -s | --suicide)
      command=SuICIDE
      break
      ;;
    -p | --panic)
      command=Panic
      break
      ;;
    # Special cases
    *)
      break
      ;;
  esac
done

echo "*** Injection Once for all tool ***"
echo ' '
if [ $command != "help" ];then
  echo "Command Selected: $command"
  echo ' '
fi

case $command in
  help)
    echo ' '
    echo 'NAME'
    echo 'injectOnce'
    echo ' '
    echo 'DESCRIPTION'
    echo 'The inject utility is used to introduce Once4All (irreversible) type of'
    echo 'injections.'
    echo ' '
    echo 'OPTIONS'
    echo ' '
    echo '-s (--suicide)'
    echo 'Shutdown the machine.'
    echo ' '
    echo '-p (--panic)'
    echo 'Cause the kernel to crash. Use caution when following these steps, and'
    echo 'by no means use them on a production machine.'
    echo ' '
    ;;
  SuICIDE)
    f_suicide
    ;;
  Panic)
    f_panic
    ;;
esac
echo 'Bye.'
echo ' '
