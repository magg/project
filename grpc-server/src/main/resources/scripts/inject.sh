#!/bin/bash
#
# inject
#
# Injection utility used to introduce noise or slowdown on CPU, Memory
# and Disk.
#
# Requires a Linux operating system for most of the methods
# Systems tested: Debian, CentOS, Oracle, RHEL, Ubuntu, Sles and Rhel
#
# Author: Francesco Sorrentino <francesco@cloudera.com>
#

command=help
pid=0

#####CPU#####

function f_burncpu(){
    if [[ -z "$1" ]]; then
      echo "Percentage parameter missing"
      echo ' '
      exit -1
    fi
    unamestr=`uname`
    if [[ "$unamestr" != 'Linux' ]]; then
      echo "Injection not supported by this OS"
      echo ' '
      return
    fi

    echo "#!/bin/bash" > /tmp/infiniteburn.sh
    echo "    while true;" >> /tmp/infiniteburn.sh
    echo "      do yes > /dev/null;" >> /tmp/infiniteburn.sh
    echo "    done" >> /tmp/infiniteburn.sh

    if(( "$1">200 || "$1"<1 ));then
        echo "Specify an amount between 0 and 200"
        echo ' '
        return
    fi

    tot=`grep -c ^processor /proc/cpuinfo`
    (( tmp= tot*$1/100 ))
    echo "Number of processors available: ${tot}"
    echo "Number of processors burned: ${tmp}"
    echo ' '
    #Should I use nice?
    for ((i=0; i<tmp; i++)); do
      nohup /bin/bash /tmp/infiniteburn.sh &
    done
}

function f_cleancpu(){
    echo "Removing CPU injection..."
    for KILLPID in `pgrep -f infiniteburn.sh`; do
      for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='$KILLPID') { print $1 }}'); do
        kill -9 "${KILLCHILDPID}";
      done
      kill -9 "${KILLPID}";
    done
    rm /tmp/infiniteburn.sh 2> /dev/null
    echo ' '
}

#####IO#####

function f_burnio(){
    if [[ -z "$1" ]]; then
      echo "Percentage parameter missing"
      echo ' '
      exit -1
    fi
    if [[ -z "$2" ]]; then
      echo "Mounting point missing"
      echo ' '
      exit -1
    fi
    tmp=$2
    mountpath=${tmp//./\/}
    mountpoint -q ${mountpath}
    if [ $? -ne 0 ]; then
      echo "The parameter is not a mountpoint"
      exit -1
    fi
    if [ "$1" -gt 0 -a "$1" -le 25 ]; then
      COUNT=1
    elif [ "$1" -gt 25 -a "$1" -le 50 ]; then
      COUNT=2
    elif [ "$1" -gt 50 -a "$1" -le 75 ]; then
      COUNT=3
    elif [ "$1" -gt 76 -a "$1" -le 100 ]; then
      COUNT=4
    else
      echo "Percentage not valid"
      echo ' '
      exit -1
    fi
    echo "Burning ${COUNT} channel(s) on ${mountpath}..."
    echo ' '
    SUFFIX=${tmp//./-}
    echo "#!/bin/bash" > /tmp/loopburnio$SUFFIX.sh
    echo "    while true; do" >> /tmp/loopburnio$SUFFIX.sh
    i=1
    while [ "$i" -le "$COUNT" ]; do
      echo "      dd if=/dev/urandom of=${mountpath}/burn${i} bs=1M count=1024 iflag=fullblock &" >> /tmp/loopburnio${SUFFIX}.sh
      i=$(($i + 1))
    done
    echo "      wait" >> /tmp/loopburnio${SUFFIX}.sh
    echo "    done" >> /tmp/loopburnio${SUFFIX}.sh

    nohup /bin/bash /tmp/loopburnio${SUFFIX}.sh &
}

function f_cleanio(){
    if [[ -z "$1" ]]; then
      echo "Mounting point missing"
      echo "Removing all IO injections..."
      for KILLPID in `pgrep -f loopburnio*`; do
        for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='${KILLPID}') { print $1 }}'); do
          kill -9 ${KILLCHILDPID};
        done
        kill -9 ${KILLPID};
      done
      rm /burn* 2> /dev/null
      rm */burn* 2> /dev/null
      rm /tmp/loopburnio* 2> /dev/null
    else
      tmp=$1
      mountpath=${tmp//./\/}
      mountpoint -q ${mountpath}
      if [ $? -ne 0 ]; then
        echo "The parameter is not a mountpoint"
        exit -1
      fi
      echo "Removing IO injection from ${mountpath}..."
      SUFFIX=${tmp//./-}
      for KILLPID in `pgrep -f loopburnio$SUFFIX.sh`; do
        for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='${KILLPID}') { print $1 }}'); do
          kill -9 ${KILLCHILDPID};
        done
        kill -9 ${KILLPID};
      done
    rm ${mountpath}/burn* 2> /dev/null
    rm /tmp/loopburnio${SUFFIX}.sh 2> /dev/null
    fi
}

############

function f_filldisk(){
    if [[ -z "$1" ]]; then
      echo "Percentage parameter missing"
      exit -1
    fi
    if [[ -z "$2" ]]; then
      echo "Mounting point missing"
      exit -1
    fi
    tmp=$2
    mountpath=${tmp//./\/}
    mountpoint -q $mountpath
    if [ $? -ne 0 ]; then
      echo "The parameter is not a mountpoint"
      exit -1
    fi
    if [ "$1" -gt 0 -a "$1" -le 100 ]; then
      tot=$(df -m $mounthpath | tail -1 | awk '{print $4}')
      echo "Space available: ${tot} Mb"
      # Calculate the percentage in MB
      counter=$(( tot * $1 / 100 ))
      echo "Space burning: ${counter}Mb on ${mountpath}..."
    else
      echo "Percentage not valid"
      exit -1
    fi
    SUFFIX=${tmp//./-}
    echo "#!/bin/bash" > /tmp/filldisk${SUFFIX}.sh
    echo "counter="${counter} >> /tmp/filldisk${SUFFIX}.sh
    echo "dd if=/dev/urandom of=${mountpath}/burndisk bs=1M count=$counter iflag=fullblock" >> /tmp/filldisk${SUFFIX}.sh

    nohup /bin/bash /tmp/filldisk${SUFFIX}.sh &
}

function f_cleanfill(){
    if [[ -z "$1" ]]; then
      echo "Mounting point missing"
      echo "Removing all DISK injections..."
      for KILLPID in `pgrep -f filldisk*`; do
        for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='$KILLPID') { print $1 }}'); do
          kill -9 ${KILLCHILDPID};
        done
        kill -9 ${KILLPID};
      done
      rm /burndisk 2> /dev/null
      rm */burndisk 2> /dev/null
      rm /tmp/filldisk* 2> /dev/null
    else
      tmp=$1
      mountpath=${tmp//./\/}
      mountpoint -q ${mountpath}
      if [ $? -ne 0 ]; then
        echo "The parameter is not a mountpoint"
        exit -1
      fi
      SUFFIX=${tmp//./-}
      echo "Removing DISK injection from ${mountpath}..."
      for KILLPID in `pgrep -f filldisk$SUFFIX.sh`; do
        for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='${KILLPID}') { print $1 }}'); do
          kill -9 ${KILLCHILDPID};
        done
        kill -9 ${KILLPID};
      done
      rm ${mountpath}/burndisk 2> /dev/null
      rm /tmp/filldisk${SUFFIX}.sh 2> /dev/null
    fi
}

############

function f_ronly(){
  if [[ -z "$1" ]]; then
    echo "Mounting point missing"
    exit -1
  fi
  tmp=$1
  mountpath=${tmp//./\/}
  mountpoint -q ${mountpath}
  if [ $? -ne 0 ]; then
    echo "The parameter is not a mountpoint"
    exit -1
  fi
  if [[ "${mountpath}" = '/' ]]; then
    echo "This injection is not revertable. AgenTEST folder will be not writable."
  fi
  echo "Mounting ${mountpath} as r-only..."
  mount -o ro,remount ${mountpath}
}

function f_cleanronly(){
  if [[ -z "$1" ]]; then
    echo "Mounting point missing"
    echo "No ReadOnly injection removed"
    exit -1
  fi
  tmp=$1
  mountpath=${tmp//./\/}
  mountpoint -q ${mountpath}
  if [ $? -ne 0 ]; then
    echo "The parameter is not a mountpoint"
    exit -1
  fi
  echo "Re-mounting ${mountpath} in rw..."
  mount -o rw,remount ${mountpath}
}

############

function f_unmount(){
  if [[ -z "$1" ]]; then
    echo "Mounting point missing"
    exit -1
  fi
  tmp=$1
  mountpath=${tmp//./\/}
  mountpoint -q ${mountpath}
  if [ $? -ne 0 ]; then
    echo "The parameter is not a mountpoint"
    exit -1
  fi
  if [[ "${mountpath}" = '/' ]]; then
    echo "This injection is not revertable. AgenTEST folder will be not accessible."
  fi
  echo "Unmounting ${mountpath}..."
  sudo unmount ${mountpath}
}

function f_cleanunmount(){
  if [[ -z "$1" ]]; then
    echo "Mounting point missing"
    echo "No unmount injection removed"
    exit -1
  fi
  tmp=$1
  mountpath=${tmp//./\/}
  mountpoint -q ${mountpath}
  if [ $? -ne 0 ]; then
    echo "The parameter is not a mountpoint"
    exit -1
  fi
  echo "Mounting ${mountpath}..."
  mount ${mountpath}
}

#####MEMORY#####

function f_fillmem(){
    if [[ -z "$1" ]]; then
      echo "Percentage parameter missing"
      exit -1
    fi
    unamestr=`uname`
    if [[ "$unamestr" != 'Linux' ]]; then
      echo "Injection not supported by this OS"
      return
    fi

    if [ ! -d "/dev/shm/" ]; then
      echo "Injection not supported by this OS"
      return
    fi

    if [ "$1" -gt 0 -a "$1" -le 100 ]; then
      tot=$(free -m | grep Mem | awk '{print $4}')
      echo "Space available: "$tot"Mb"
      counter=$(( tot * $1 / 100 * 1024 ))
      echo "Space filled: "$((counter/1024))"Mb"
    else
      echo "Percentage not valid"
      exit -1
    fi

    echo "#!/bin/bash" > /tmp/fillmemory.sh
    echo "counter="${counter} >> /tmp/fillmemory.sh
    echo "dd if=/dev/urandom of=/dev/shm/fill bs=1k count=$counter iflag=fullblock" >> /tmp/fillmemory.sh

    nohup /bin/bash /tmp/fillmemory.sh &
}

function f_cleanmem(){
    echo "Removing MEMORY injection..."
    for KILLPID in `pgrep -f 'fillmemory.sh'`; do
      for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='$KILLPID') { print $1 }}'); do
        kill -9 ${KILLCHILDPID};
      done
      kill -9 ${KILLPID};
    done
    rm /dev/shm/fill 2> /dev/null
    rm /tmp/fillmemory.sh 2> /dev/null
}

############

function f_hang(){
    echo "Starting forkbomb..."
    echo "#!/bin/bash" > /tmp/forkbomb.sh
    echo ":(){ :|:& };:" >> /tmp/forkbomb.sh

    nohup /bin/bash /tmp/forkbomb.sh &
}

function f_cleanhang(){
    echo "Cleaning forkbomb..."
    for KILLPID in `pgrep -f forkbomb.sh`; do
      for KILLCHILDPID in $(ps -o pid,ppid ax | awk '{ if ($2=='$KILLPID') { print $1 }}'); do
        kill -9 ${KILLCHILDPID};
      done
      kill -9 ${KILLPID};
    done
    rm /tmp/forkbomb.sh 2> /dev/null
}

############

function f_cleanall(){
    unamestr=`uname`
    if [[ "$unamestr" == 'Linux' ]]; then
      f_cleancpu
      f_cleanmem
    fi
    f_cleanfill
    f_cleanio
    f_cleanhang
    echo "WARNING: ROnly and Unmount need to be removed manually."
}

############

while test $# -gt 0
do
  case $1 in
    -h | --help)
      # usage and help
      command=help
      break
      ;;
    -b | --burncpu)
      command=burnCPU
      shift
      num=$1
      break
      ;;
    --cleancpu)
      command=cleanCPU
      break
      ;;
    -s | --stop)
      command=stop
      shift
      pid=$1
      break
      ;;
    -c | --cont)
      command=cont
      shift
      pid=$1
      break
      ;;
    -i | --burnio)
      command=burnIO
      shift
      percentage=$1
      mountingpoint=$2
      break
      ;;
    --cleanio)
      command=cleanIO
      shift
      mountingpoint=$1
      break
      ;;
    -f | --filldisk)
      command=fillDISK
      shift
      percentage=$1
      mountingpoint=$2
      break
      ;;
    --cleanfill)
      command=cleanDISK
      shift
      mountingpoint=$1
      break
      ;;
    -a | --hang)
      command=hang
      break
      ;;
    --cleanhang)
      command=cleanhang
      break
      ;;
    -m | --fillmem)
      command=fillMEM
      shift
      amt=$1
      break
      ;;
    --cleanmem)
      command=cleanMEM
      break
      ;;
    -r | --ronly)
      command=rONLY
      shift
      mountingpoint=$1
      break
      ;;
    --cleanreadonly)
      command=cleanrONLY
      shift
      mountingpoint=$1
      break
      ;;
    -u | --unmount)
      command=unMOUNT
      shift
      mountingpoint=$1
      break
      ;;
    --cleanunmount)
      command=cleanunMOUNT
      shift
      mountingpoint=$1
      break
      ;;
    --cleanall)
      command=cleanALL
      break
      ;;
      # Special cases
    *)
      break
      ;;
  esac
done
    ####################################################
echo "************  Injection tool CPU/MEM/IO  ***********"
echo ' '
if [ $command != "help" ];then
  echo "Command Selected: $command"
  echo ' '
fi

case $command in
  help)
    echo ' '
    echo 'NAME'
    echo 'inject'
    echo ' '
    echo 'DESCRIPTION'
    echo 'The inject utility is used to introduce noise or slowdown on CPU, '
    echo 'Memory and Disk.'
    echo ' '
    echo 'OPTIONS'
    echo '-b (--burncpu) amount'
    echo 'Run CPU intensive processes, simulating a noisy neighbor or a faulty CPU.'
    echo 'The instance will effectively have a much slower CPU. It runs n parallel'
    echo '100% CPU tasks. amount is a value between 1 and 200, and represents the '
    echo 'percentage of CPU to burn. That is 50 will results in half of the CPU '
    echo 'available burned, 100 will uses all the CPUs (values bigger than 100 '
    echo 'stress the system significatively).'
    echo ' '
    echo '--cleancpu'
    echo 'Interrupt the injection introduced by burncpu.'
    echo ' '
    echo '-s (--stop) pid'
    echo 'A SIGSTOP signal is sent to a process, the usual behaviour is to pause'
    echo 'that process in its current state.'
    echo 'The process will only resume execution if it is sent the SIGCONT signal'
    echo '(using cont option). SIGSTOP cannot be caught or ignored.'
    echo ' '
    echo '-c (--cont) pid'
    echo 'Resume a process previously paused using the SIGSTOP signal'
    echo ' '
    echo '-i (--burnio) amount mount_point'
    echo 'Run disk intensive processes, simulating a noisy neighbor or a faulty'
    echo 'disk. The instance will effectively have a much slower disk. It runs n'
    echo 'parallel BURNIO tasks. amount is a value between 1 and 100, and represents'
    echo 'the percentage of IO to burn (4 processes max). That is 50 will results in'
    echo '2 BURNIO tasks running.'
    echo 'mount_point indicates the device where the burn will occur.'
    echo ' '
    echo '--cleanio mount_point'
    echo 'Interrupt the injection introduced by burnio.'
    echo 'mount_point indicates the device where the burn is occurring, if no'
    echo 'mount_point specified all the burio injection will be removed'
    echo ' '
    echo '-f (--filldisk) amount mount_point'
    echo 'Write a huge file to the root device, filling up the root disk.'
    echo 'amount is a value between 1 and 100, and represents'
    echo 'the percentage of DISK available to burn.'
    echo 'mount_point indicates the device where the burn will occur.'
    echo ' '
    echo '--cleanfill mount_point'
    echo 'Relase the space used by filldisk and interrupt the injection.'
    echo 'mount_point indicates the device where the burn is occurring, if no'
    echo 'mount_point specified all the burio injection will be removed'
    echo ' '
    echo '-a | (--hang)'
    echo 'Start a forkbomb. That is a process continually replicates itself to'
    echo 'deplete available system resources, causing resource starvation and'
    echo 'slowing or crashing the system.'
    echo ' '
    echo '--cleanhang'
    echo 'Stop the forkbomb injection.'
    echo ' '
    echo '-m (--fillmem) amount'
    echo 'Burn memory space, amount is a value between 1 and 100, and represents'
    echo 'the percentage of Memory available to burn.'
    echo ' '
    echo '--cleanmem'
    echo 'Relase the memory used by fillmem and interrupt the injection.'
    echo ' '
    echo '-r | (--ronly) mount_point'
    echo 'Mount the mounting point passed as parameter in read only mode.*'
    echo ' '
    echo '--cleanreadonly mount_point'
    echo 'Re-mount the mounting point passed as parameter in rw mode.'
    echo ' '
    echo '-u | (--unmount) mount_point'
    echo 'Unmount the mounting point passed as parameter.*'
    echo ' '
    echo '--cleanunmount mount_point'
    echo 'Re-mount the mounting point passed as parameter.'
    echo ' '
    echo '--cleanall'
    echo 'Release the resourses and interrupts all the injections.'
    echo ' '
    echo '* These injections are not reversible if applied to /'
    echo ' '
    ;;
  burnCPU)
    f_burncpu ${num}
    ;;
  cleanCPU)
    f_cleancpu
    ;;
  stop)
    if(( "$pid"<2 ));then
      echo "Specify a valid pid"
    return
    fi
    kill -SIGSTOP ${pid}
    ;;
  cont)
    kill -SIGCONT ${pid}
    ;;
  burnIO)
    f_burnio ${percentage} ${mountingpoint}
    ;;
  cleanIO)
    f_cleanio ${mountingpoint}
    ;;
  fillDISK)
    f_filldisk ${percentage} ${mountingpoint}
    ;;
  cleanDISK)
   f_cleanfill ${mountingpoint}
   ;;
  fillMEM)
    f_fillmem ${amt}
    ;;
  cleanMEM)
    f_cleanmem
    ;;
  hang)
    f_hang
    ;;
  cleanhang)
    f_cleanhang
    ;;
  rONLY)
    f_ronly ${mountingpoint}
    ;;
  cleanrONLY)
    f_cleanronly ${mountingpoint}
    ;;
  unMOUNT)
    f_unmount ${mountingpoint}
    ;;
  cleanunMOUNT)
    f_cleanunmount ${mountingpoint}
    ;;
  cleanALL)
    f_cleanall
    ;;
esac
echo 'Bye.'
echo ' '
