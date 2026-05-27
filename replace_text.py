import shutil

# Files to process
files_to_fix = [
    'D:/workspace/ai/code/eduverse/index.a687b3ad.js',
    'D:/workspace/ai/code/eduverse/frontend/tj-portal/assets/index.a687b3ad.js',
]

old = b'\\u5929\\u673AAI\\u52A9\\u7406'
new = b'EduVerse AI\\u52A9\\u7406'

for fpath in files_to_fix:
    # Backup
    shutil.copy2(fpath, fpath + '.bak')

    with open(fpath, 'rb') as f:
        content = f.read()

    # Count occurrences before replacement
    count_before = content.count(old)
    print(f"File: {fpath}")
    print(f"  Occurrences before replacement: {count_before}")

    # Replace
    content = content.replace(old, new)

    # Verify
    count_after = content.count(old)
    count_new = content.count(new)
    print(f"  Occurrences after replacement (old): {count_after}")
    print(f"  Occurrences after replacement (new): {count_new}")

    # Write back
    with open(fpath, 'wb') as f:
        f.write(content)

    print(f"  File updated successfully")
    print()

print("Done! Backup files saved with .bak extension")
