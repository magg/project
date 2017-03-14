#!/bin/bash
#
# partition
#
# partition utility used to partition the network
#
# Requires a Linux operating system.
#
# Other requirements:
# 2 files: .server-list1 and .server-list2 containing the hosts that should be in
# the two partitions
# passwordless hosts
# root privileges
#
# Author: Francesco Sorrentino <francesco@cloudera.com>
#

command=help

while test $# -gt 0
do
  case $1 in
    -h | --help)
      # usage and help
      command=help
      break
      ;;
    -c | --create)
      command=addpartition
      break
      ;;
    -d | --destroy)
      command=delpartition
      break
      ;;
    -s | --status)
      command=status
      break
      ;;
    *)
      break
      ;;
  esac
done

case $command in
  help)
    echo ' '
    echo 'NAME'
    echo 'partition'
    echo ' '
    echo 'DESCRIPTION'
    echo 'Utility used to partition the network.'
    echo ' '
    echo 'REQUIREMENTS'
    echo '- file .server-list1 containing the hosts that will belong to the first '
    echo '  partition.'
    echo '- file .server-list2 containing the hosts that will belong to the second '
    echo '  partition.'
    echo '- passwordless hosts.'
    echo '- root privileges.'
    echo ' '
    echo 'OPTIONS'
    echo '-c (--create)'
    echo 'Create the partition.'
    echo ' '
    echo '-d (--destroy)'
    echo 'Destroy the partition.'
    echo ' '
    echo '-s (--status)'
    echo 'Show the partition.'
    echo ' '
    exit 0
    ;;
  addpartition)
    flagOp="A"
    ;;
  delpartition)
    flagOp="D"
    ;;
  status)
    flagOp="S"
    ;;
esac

# File containing hostnames for all the servers belonging to the first partition;
# one per line.
serverlistP1=.server-list1

# File containing hostnames for all the servers belonging to the second partition;
# one per line.
serverlistP2=.server-list2

# Check if the files are readable
[ -r "$serverlistP1" ] || (echo "file .server-list1 not existing" ; exit 1)
[ -r "$serverlistP2" ] || (echo "file .server-list2 not existing" ; exit 1)

# List of hosts to connect to

while read host dummy ; do

    # Ignore comments
    host="${host%%;*}"
    host="${host%%#*}"
    host="${host%%/*}"
    [ -n "$host" ] || continue

    # Add host to hosts used
    hosts1=("${hosts1[@]}" "$host")
done < "$serverlistP1" || exit $?

# None?
[ ${#hosts1[@]} -gt 0 ] || (echo "file .server-list1 empty" ; exit 0)


hosts2=()
while read host dummy ; do

    # Ignore comments
    host="${host%%;*}"
    host="${host%%#*}"
    host="${host%%/*}"
    [ -n "$host" ] || continue

    # Add host to hosts used
    hosts2=("${hosts2[@]}" "$host")
done < "$serverlistP2" || exit $?


# None?
[ ${#hosts2[@]} -gt 0 ] || (echo "file .server-list2 empty" ; exit 0)

# Create a safe, autodeleted temporary directory
work=$(mktemp -d) || exit $?
trap "rm -rf '$work'" EXIT

# Run remote commands in parallel on each host in .server-list1.
for host1 in "${hosts1[@]}" ; do
    (   cmds=()
        if [ "$flagOp" = "S" ]; then
            cmds="iptables -L"
        else
            for host2 in "${hosts2[@]}" ; do
                cmds=$cmds" iptables -"$flagOp" INPUT -s "$host2" -j DROP ;"
            done
        fi
        exec </dev/null >"$work/$host1.out" 2>"$work/$host1.err"
        ssh "root@$host1" "$cmds" && echo "[OK]" || echo "[Error $?]"
    ) &
done

# Run remote commands in parallel on each host in .server-list2.
for host2 in "${hosts2[@]}" ; do
    (   cmds=()
        if [ "$flagOp" = "S" ]; then
            cmds="iptables -L"
        else
            for host1 in "${hosts1[@]}" ; do
                cmds=$cmds" iptables -"$flagOp" INPUT -s "$host1" -j DROP ;"
            done
        fi
        exec </dev/null >"$work/$host2.out" 2>"$work/$host2.err"
        ssh "root@$host2" "$cmds" && echo "[OK]" || echo "[Error $?]"
    ) &
done

# Wait for all to complete
wait

# Output the summary of all commands
for host in "${hosts1[@]}" ; do
    echo "Output from $host:"
    sed -e 's|^|\t|' "$work/$host.out"
    echo ""
    if [ -s "$work/$host.err" ]; then
        echo "Errors from $host:"
        sed -e 's|^|\t|' "$work/$host.err"
    else
        echo "No error messages."
    fi
    echo ""
    echo "========================================"
done


# Output the summary of all commands
for host in "${hosts2[@]}" ; do
    echo "Output from $host:"
    sed -e 's|^|\t|' "$work/$host.out"
    echo ""
    if [ -s "$work/$host.err" ]; then
        echo "Errors from $host:"
        sed -e 's|^|\t|' "$work/$host.err"
    else
        echo "No error messages."
    fi
    echo ""
    echo "========================================"
done
