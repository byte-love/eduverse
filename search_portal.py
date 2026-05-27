import paramiko

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('192.168.150.101', username='root', password='123321', timeout=10)

# Search for the text using hex patterns in perl
cmd = r"""
echo "=== Searching for 天机AI助理 ==="
find /usr/local/src/tj-portal -type f | while read f; do
    if hexdump -e '16/1 "%02X "' "$f" 2>/dev/null | grep -q "E5A4A9E69CBA4149E58AA9E790"; then
        echo "FOUND in: $f"
    fi
done
echo "=== Searching for 天机 ==="
find /usr/local/src/tj-portal -type f | while read f; do
    if hexdump -e '16/1 "%02X "' "$f" 2>/dev/null | grep -q "E5A4A9E69CBA"; then
        echo "FOUND in: $f"
    fi
done
echo "DONE"
"""

stdin, stdout, stderr = c.exec_command(cmd)
print(stdout.read().decode('utf-8', errors='replace'))
print(stderr.read().decode('utf-8', errors='replace'))
c.close()
