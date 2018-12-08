<%@ page pageEncoding="utf-8"%>
<html>
<body>
<h2>MMALL</h2>

springmvc上传文件
<form action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <%--这里的name要和Controller中@RequestParam(value = "upload_file", required = false) MultipartFile file注解里的名称保持一致--%>
    <input type="file" name="upload_file"/>
    <input type="submit" value="上传文件"/>
</form>

富文本上传文件
<form action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <%--这里的name要和Controller中@RequestParam(value = "upload_file", required = false) MultipartFile file注解里的名称保持一致--%>
    <input type="file" name="upload_file"/>
    <input type="submit" value=" 上传文件"/>
</form>
</body>
</html>
