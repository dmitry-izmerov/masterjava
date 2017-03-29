<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>File Upload Sample</title>
</head>
<body>
<form action="/upload" enctype="multipart/form-data" method="post">
    <p>
        <label>Select a file: </label>
        <input type="file" name="file"/>
    </p>
    <input type="submit" value="Upload" />
    <c:if test="${not empty users}">
        <hr>
        <h2>Imported users:</h2>
        <table border="1" cellspacing="0" cellpadding="5">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Flag</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${users}" var="user">
                <tr>
                    <td>${user.value}</td>
                    <td>${user.email}</td>
                    <td>${user.flag}</td>
                </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>
</form>
</body>
</html>