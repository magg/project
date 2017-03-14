inject.sh
========

DESCRIPTION

The inject utility is used to introduce noise or slowdown on CPU, 
Memory and Disk.

The utility has been tested for: Debian, CentOS, Oracle, RHEL, Ubuntu, Sles and Rhel
 
OPTIONS

-b (--burncpu) amount

Run CPU intensive processes, simulating a noisy neighbor or a faulty CPU.
The instance will effectively have a much slower CPU. It runs n parallel
100% CPU tasks. amount is a value between 1 and 200, and represents the 
percentage of CPU to burn. That is 50 will results in half of the CPU 
available burned, 100 will uses all the CPUs (values bigger than 100 
stress the system significatively).
 
--cleancpu

Interrupt the injection introduced by burncpu.
 
-s (--stop) pid

A SIGSTOP signal is sent to a process, the usual behaviour is to pause 
that process in its current state. 
The process will only resume execution if it is sent the SIGCONT signal 
(using cont option). SIGSTOP cannot be caught or ignored.
 
-c (--cont) pid

Resume a process previously paused using the SIGSTOP signal.
 
-i (--burnio) amount mount_point

Runs disk intensive processes, simulating a noisy neighbor or a faulty disk.
The instance will effectively have a much slower disk. It runs n parallel BURNIO
tasks.
amount is a value between 1 and 100, and represents the percentage of IO
to burn (4 processes max). That is 50 will results in 2 BURNIO tasks running.
mount_point indicates the device where the burn will occur.
 
--cleanio mount_point

Interrupt the injection introduced by burnio.
mount_point indicates the device where the burn is occurring, if no mount_point
specified all the burio injection will be removed.

-f (--filldisk) amount mount_point

Write a huge file to the root device, filling up the root disk.
amount is a value between 1 and 100, and represents the percentage of DISK available
to burn.
mount_point indicates the device where the burn will occur.
 
--cleanfill mount_point

Relase the space used by filldisk and interrupt the injection.
mount_point indicates the device where the burn is occurring, if no mount_point
specified all the burio injection will be removed.
 
-m (--fillmem) amount

Burns memory space. 
amount is a value between 1 and 100, and represents the percentage of Memory available to burn.
 
--cleanmem

Relase the memory used by fillmem and interrupt the injection.
 
-u (--suicide)

Shutdown the local machine.

--cleanall

Relase the resourses and interrupts all the injections.


partition.sh
========

DESCRIPTION

Utility used to partition the network.

REQUIREMENTS

- file .server-list1 containing the hosts that will belong to the first 
partition.
- file .server-list2 containing the hosts that will belong to the second 
partition.
- passwordless hosts.
- root privileges.

OPTIONS

-c (--create)

Create the partition.

-d (--destroy)

Destroy the partition.

-s (--status)

Show the partition.

Note: Recursively calling (adjusting the host-list files) more than 2 partitions
can be created.


agentest.sh
========

DESCRIPTION

Start/stop agentest daemon.

REQUIREMENTS

- daemonize (http://software.clapper.org/daemonize/) adviced but not mandatory.

ENVIRONMENT

BINDIR - bin path

LIBDIR - lib/jar path

LOGDIR - path to log output, error, etc.

JAVA_HOME - path to java home

JAVA_BIN - path to java binary

CLASSPATH - classpath associated with agentest

CLASS - main class to run

PIDFILE - PID file path

WATCHDOGDIR - path for watchdog thread to watch

DAEMONIZEBIN - path to daemonize binary

USAGE

    agentest.sh [start|stop|restart]
