import sys

with open('D:/workspace/ai/code/eduverse/index.a687b3ad.js', 'rb') as f:
    content = f.read()

print("File size:", len(content))

# Search for dialogHeader context
idx = content.find(b'dialogHeader')
if idx >= 0:
    print(f"\n=== 'dialogHeader' found at position {idx} ===")
    # Get a window around it
    chunk = content[max(0,idx-500):idx+2000]
    # Find all occurrences of class:"font" or similar nearby
    for i in range(len(chunk) - 20):
        if chunk[i:i+10] == b'class:"font':
            print(f"  'class:\"font' at chunk offset {i}: {chunk[i:i+120]}")

# Search for the hex bytes of the specific Unicode escapes
# In a JS file, 天 is stored as literal: 5C 75 35 39 32 39
text_to_find = bytes([0x5C, 0x75, 0x35, 0x39, 0x32, 0x39,  # 天
                      0x5C, 0x75, 0x36, 0x37, 0x33, 0x61,  # 机
                      0x41, 0x49,                            # AI (literal ASCII)
                      0x5C, 0x75, 0x35, 0x32, 0x61, 0x39,  # 助
                      0x5C, 0x75, 0x37, 0x34, 0x30, 0x36]) # 理

pos = content.find(text_to_find)
print(f"\n{'='*60}")
print(f"Hex-constructed pattern: {'FOUND at ' + str(pos) if pos >= 0 else 'NOT FOUND'}")
if pos >= 0:
    print(f"Context: {content[max(0,pos-100):pos+len(text_to_find)+100]}")

# Let me also search the REPO version to compare
print("\n\n=== Comparing with repo version ===")
try:
    with open('D:/workspace/ai/code/eduverse/frontend/tj-portal/assets/index.a687b3ad.js', 'rb') as f2:
        repo_content = f2.read()
    print(f"Repo file size: {len(repo_content)}")
    print(f"Files identical: {content == repo_content}")
except FileNotFoundError:
    print("Repo file not found at expected path")

# Search for all occurrences of class:\"font\"
print("\n=== All class:\"font\" occurrences ===")
font_positions = []
pos_search = -1
while True:
    pos_search = content.find(b'class:\"font\"', pos_search + 1)
    if pos_search < 0:
        break
    font_positions.append(pos_search)

print(f"Found {len(font_positions)} occurrences of class:\"font\"")
for i, p in enumerate(font_positions[:10]):
    print(f"  {i}: pos {p}: ...{content[p:p+150]}...")

# Also search for string: "EduVerse" to find the greeting
print("\n=== Search for EduVerse ===")
for term in [b'EduVerse', b'Eduverse', b'eduverse']:
    pos_term = content.find(term)
    print(f"  {term.decode()}: {'FOUND at ' + str(pos_term) if pos_term >= 0 else 'NOT FOUND'}")

# Also search for Nacos config title that was already changed
# The greeting "Hello，我是EduVerse的AI助理" - search in various forms
print("\n=== Search for greeting text ===")
for term in [b'Hello\\uff0c\\u6211\\u662f', b'Hello', b'EduVerse\\u7684AI\\u52a9\\u7406']:
    pos_term = content.find(term)
    result = f"FOUND at {pos_term}" if pos_term >= 0 else "NOT FOUND"
    print(f"  {term[:50]}: {result}")
