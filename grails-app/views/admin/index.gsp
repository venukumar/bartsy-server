<%@ page import="bartsy.*"%>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main"/>
    <title><g:message code="page.admin.login.title" default="Admin Login" /></title>
  </head>
  
<body>
	<div style="height:300px;padding-top:100px;">
        <g:if test="${flash.message}">
          <div class="message" id="ferr" role="status">${flash.message}</div>
        </g:if>
        <g:if test="${flash.errors}">
          <div class="errors" id="ferr">${flash.errors}</div>
        </g:if>
       
		<div class="error-msg" id="err" style="display: none"></div>
        <g:hasErrors bean="${adminUserInstance}">
          <ul class="errors" role="alert">
            <g:eachError bean="${adminUserInstance}" var="error">
              <li>
              <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>
              <g:message error="${error}" />
              </li>
            </g:eachError>
          </ul>
        </g:hasErrors>
        <div class="error-msg" id="msg_error"></div>
        <% flash.clear() %>
      
      <g:form action="adminLogin" method="post">
   	  <div class="login_fields" class="${hasErrors(bean: adminUserInstance, field: 'username', 'error')} required">
			<g:message code="adminUser.username.label" default="Username" /><span class="required-indicator">*</span>
			<input type="text" name="username" id="username" value="${adminUserInstance?.username}" class="txt-field required" />
      </div>
      
      <div class="login_fields" class="${hasErrors(bean: adminUserInstance, field: 'password', 'error')} required">
			<g:message code="adminUser.pwd.label" default="Password" /><span class="required-indicator">*</span>
			<input type="password" name="password" id="password" value="${adminUserInstance?.password}" class="txt-field required" />
      </div>
      
      <div class="button-con">
		<input name="login" type="submit" value="${message(code: 'loginbutton.label', default: 'Login')}" />
      </div>
      </g:form>
	</div>
</body>
</html>	