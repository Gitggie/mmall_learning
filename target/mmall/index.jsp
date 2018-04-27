<%@page pageEncoding="UTF-8"%>
<html>
<body>
<h2>Hello World!</h2>

springmvc上传文件
<%--form 设置了 enctype="multipart/form-data" 属性后，就是 2 进制传输数据了--%>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="springmvc上传文件"/>
</form>


富文本图片上传文件
<form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="富文本图片上传文件"/>
</form>

</body>
</html>
