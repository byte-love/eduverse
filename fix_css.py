import shutil

css_path = 'D:/workspace/ai/code/eduverse/index.b533a0f3.css'
repo_css = 'D:/workspace/ai/code/eduverse/frontend/tj-portal/assets/index.b533a0f3.css'

for fpath in [css_path, repo_css]:
    shutil.copy2(fpath, fpath + '.bak')

    with open(fpath, 'rb') as f:
        content = f.read()

    # The relevant CSS rules to modify:
    # 1. [data-v-58ea3bf8] .superDialog{width:460px;...}
    # 2. [data-v-58ea3bf8] .superDialog .el-overlay-dialog{width:480px;...}

    # Replace superDialog width: 460px -> 550px
    old1 = b'[data-v-58ea3bf8] .superDialog{width:460px;'
    new1 = b'[data-v-58ea3bf8] .superDialog{width:550px;'
    count1 = content.count(old1)
    content = content.replace(old1, new1)

    # Replace el-overlay-dialog width: 480px -> 570px
    old2 = b'[data-v-58ea3bf8] .superDialog .el-overlay-dialog{width:480px;'
    new2 = b'[data-v-58ea3bf8] .superDialog .el-overlay-dialog{width:570px;'
    count2 = content.count(old2)
    content = content.replace(old2, new2)

    with open(fpath, 'wb') as f:
        f.write(content)

    print(f'{fpath}:')
    print(f'  superDialog 460->550: {count1} replaced')
    print(f'  el-overlay-dialog 480->570: {count2} replaced')

print('Done!')
