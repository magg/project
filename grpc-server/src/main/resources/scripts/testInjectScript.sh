#!/bin/bash
#
# This script validate the FI tools.
#
# Author: Francesco Sorrentino <francesco@cloudera.com>

# If the second parameter is 0 we expected the command to succed, if it is 1 we
# expected the command to fail
function check_actual_expected(){
    if [[ $2 == 0 ]]; then
        if [[ $1 == 0 ]]; then
            echo "TEST STATUS PASSED"
        else
            echo "TEST STATUS FAILED"
        fi
    else
        if [[ $1 == 0 ]]; then
            echo "TEST STATUS FAILED"
        else
            echo "TEST STATUS PASSED"
        fi
    fi
}

function check_number_zero(){
    if [[ $1 == 0 ]]; then
        echo "TEST COUNT PASSED"
    else
        echo "TEST COUNT FAILED"
    fi
}

function check_number_nzero(){
    if [[ $1 == 0 ]]; then
        echo "TEST COUNT FAILED"
    else
        echo "TEST COUNT PASSED"
    fi
}

function check_which(){
    tmp=$(which $1)
    if [[ ! -z ${tmp} ]]; then
        echo "TEST PASSED "$1 | tr [a-z] [A-Z]
    else
        echo "TEST FAILED "$1 | tr [a-z] [A-Z]
    fi
}

function check_dir(){
    if [[ -w $1 ]]; then
        echo "TEST PASSED "$1
    else
        echo "TEST FAILED "$1
    fi
}

function check_env_var(){
    tmp=$(echo ${JAVA_HOME})
    if [[ ! -z ${tmp} ]]; then
        echo "TEST PASSED JAVA_HOME"
    else
        echo "TEST FAILED JAVA_HOME"
    fi
    echo " "
    tmp=$(echo ${JAVA_BIN})
    if [[ ! -z ${tmp} ]]; then
        echo "TEST PASSED JAVA_BIN"
    else
        echo "TEST FAILED JAVA_BIN"
    fi
}

echo "####################################################"
echo "#           T E S T I N G     S E T U P            #"
echo "#                                                  #"
echo "#                 A g e n T E S T                  #"
echo "#                                                  #"
echo "####################################################"
echo "#                                                  #"
echo "# Warning: kernelPanic and forkBomb are not tested #"
echo "#                                                  #"
echo "####################################################"

echo " "
inject.sh --burncpu
result=$?
count=$(ps aux | grep infiniteburn | grep -v grep | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}

echo " "
inject.sh --burncpu 50
result=$?
sleep 10
count=$(ps aux | grep infiniteburn  | grep -v grep | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}

echo " "
inject.sh --cleancpu
count=$(ps aux | grep infiniteburn  | grep -v grep | wc -l)
check_number_zero ${count}

echo " "
echo "####################################################"
echo " "

inject.sh --burnio
result=$?
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --burnio 2
result=$?
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --burnio 2 "."
result=$?
sleep 10
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}
check_number_nzero ${count1}

echo " "
inject.sh --cleanio "."
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --burnio 2 "."
result=$?
sleep 10
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}
check_number_nzero ${count1}

echo " "
inject.sh --cleanio
count=$(ps aux | grep loop | grep -v grep | wc -l)
count1=$(ls -l /burn* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}

echo " "
echo "####################################################"
echo " "

inject.sh --filldisk
result=$?
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --filldisk 1
result=$?
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --filldisk 1 "."
result=$?
sleep 10
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}
check_number_nzero ${count1}

echo " "
inject.sh --cleanfill "."
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --filldisk 1 "."
result=$?
sleep 10
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}

echo " "
inject.sh --cleanfill
count=$(ps aux | grep filldisk | grep -v grep | wc -l)
count1=$(ls -l /burndisk* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}

echo " "
echo "####################################################"
echo " "

inject.sh --fillmem
result=$?
count=$(ps aux | grep fillmemory | grep -v grep | wc -l)
count1=$(ls -l /dev/shm/* 2> /dev/null | wc -l)
check_actual_expected ${result} 1
check_number_zero ${count}
check_number_zero ${count1}

echo " "
inject.sh --fillmem 2
result=$?
count=$(ps aux | grep fillmemory | grep -v grep | wc -l)
count1=$(ls -l /dev/shm/* 2> /dev/null | wc -l)
check_actual_expected ${result} 0
check_number_nzero ${count}
check_number_nzero ${count1}

echo " "
inject.sh --cleanmem
count=$(ps aux | grep fill | grep -v grep | wc -l)
count1=$(ls -l /dev/shm/* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}

echo " "
echo "####################################################"
echo " "

inject.sh --burnio 2 "."
inject.sh --filldisk 1 "."
inject.sh --burncpu 50
inject.sh --fillmem 2
sleep 10
count=$(ps aux | grep fill | grep -v grep | wc -l)
count1=$(ps aux | grep loop | grep -v grep | wc -l)
count2=$(ls -l /burn* 2> /dev/null | wc -l)
count3=$(ls -l /dev/shm/* 2> /dev/null | wc -l)
check_number_nzero ${count}
check_number_nzero ${count1}
check_number_nzero ${count2}
check_number_nzero ${count3}

echo " "
inject.sh --cleanall
count=$(ps aux | grep fill | grep -v grep | wc -l)
count1=$(ps aux | grep loop | grep -v grep | wc -l)
count2=$(ls -l /burn* 2> /dev/null | wc -l)
count3=$(ls -l /dev/shm/* 2> /dev/null | wc -l)
check_number_zero ${count}
check_number_zero ${count1}
check_number_zero ${count2}
check_number_zero ${count3}

echo " "
echo "####################################################"
echo " "

check_which daemonize
echo " "
check_which iptables
echo " "
check_which tc
echo " "
check_which ip
echo " "
check_which git
echo " "
check_which mvn
echo " "
check_which java
echo " "
check_which dd
echo " "
check_which pssh
echo " "
check_which parallel-ssh

echo " "
echo "####################################################"
echo " "

check_dir /dev/shm
echo " "
check_dir /dev/urandom
echo " "
check_dir /proc/sys/kernel/sysrq
echo " "
check_dir /proc/sysrq-trigger
echo " "
check_dir /tmp

echo " "
echo "####################################################"
echo " "

check_env_var

echo " "
echo "####################################################"
echo " "
