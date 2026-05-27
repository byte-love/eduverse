import sys

with open('D:/workspace/ai/code/eduverse/index.a687b3ad.js', 'rb') as f:
    content = f.read()

# Get the full context around position 2478108
pos = 2478108
print("=== Full context around the AI assistant menu bar ===")
# Get a larger window to see the full component structure
start = max(0, pos - 500)
end = min(len(content), pos + 2000)
chunk = content[start:end]
print(f"Window: bytes {start} to {end} ({len(chunk)} bytes)")
print()

# Try to decode as ASCII, replacing non-ASCII
decoded = chunk.decode('ascii', errors='replace')
print(decoded[:3000])

print("\n\n=== Now looking for specific patterns ===")

# The old text
old_text = b'\\u5929\\u673AAI\\u52A9\\u7406'
print(f"\nOld text bytes: {old_text}")
print(f"Old text decoded: {old_text.decode('ascii')}")

# Search for all occurrences
count = 0
pos_search = -1
while True:
    pos_search = content.find(old_text, pos_search + 1)
    if pos_search < 0:
        break
    count += 1
    print(f"  Occurrence {count} at position {pos_search}")

# Also check the admin JS
print("\n=== Checking admin portal JS ===")
try:
    with open('D:/workspace/ai/code/eduverse/frontend/tj-admin/assets/index.a073b180.js', 'rb') as f:
        admin_content = f.read()
    pos_admin = admin_content.find(old_text)
    if pos_admin >= 0:
        print(f"FOUND in admin at position {pos_admin}")
        print(f"Context: {admin_content[max(0,pos_admin-100):pos_admin+len(old_text)+100]}")
    else:
        print("NOT FOUND in admin portal JS")

    # Also check the VM admin file
    print("\n=== Checking VM admin portal JS ===")
    import paramiko
    c = paramiko.SSHClient()
    c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    c.connect('192.168.150.101', username='root', password='123321', timeout=10)
    sftp = c.open_sftp()
    local_admin = 'D:/workspace/ai/code/eduverse/vm_admin_index.a073b180.js'
    sftp.get('/usr/local/src/tj-admin/assets/index.a073b180.js', local_admin)
    sftp.close()
    c.close()
    with open(local_admin, 'rb') as f:
        vm_admin = f.read()
    pos_vm_admin = vm_admin.find(old_text)
    if pos_vm_admin >= 0:
        print(f"FOUND in VM admin at position {pos_vm_admin}")
        print(f"Context: {vm_admin[max(0,pos_vm_admin-100):pos_vm_admin+len(old_text)+100]}")
    else:
        print("NOT FOUND in VM admin portal JS")
    print(f"VM admin file size: {len(vm_admin)}")
except Exception as e:
    print(f"Error: {e}")
