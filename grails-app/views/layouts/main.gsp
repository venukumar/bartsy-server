<%@page import="bartsy.*" contentType="text/html;charset=UTF-8" %>
<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="Bartsy"/></title>
    <meta content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0' name='viewport' />
	<meta name="viewport" content="width=device-width" />
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
    <g:javascript library="application" /> 
    <script src="${resource(dir: 'js', file: 'jquery-1.10.2.min.js')}" type="text/javascript"></script>
	<modalbox:modalIncludes />
 	<g:layoutHead/>
 	 <r:require module="export"/>
  	<r:layoutResources />
  	
  	</head>
  <body>
  <div id="wrappar">
  <div id="main-container">
  
  <div class="header-con">
  	<div class="logo"><h1>VENDSY</h1></div>
  	<!--  <div class="welcme">Bartsy Dashboard</div>-->
  </div>
  <g:if test="${session.user}">
 
 	<div class="main_navigation">
 		<ul>
			<li><g:link controller="admin" action="ordersList"><g:message code="default.home.label" default="Home" class="active"/></g:link></li> 
			<li><g:link controller="admin" action="usersList"><g:message code="user.label" default="Users" class="active" /></g:link></li>
			<!--  <li><g:link controller="admin" action="saleuserList"><g:message code="venue.label" default="Venues" class="active" /></g:link></li>-->
            <li><g:link controller="admin" action="venueList"><g:message code="venue.label" default="Venues" class="active" /></g:link></li>
            <li><g:link controller="admin" action="#"><g:message code="reports.label" default="Reports" class="active" /></g:link></li>
            <li><g:link controller="admin" action="#"><g:message code="promotion.label" default="Promotions" class="active" /></g:link></li>
            <li><g:link controller="admin" action="appSettings"><g:message code="settings.label" default="Settings" class="active" /></g:link></li>
		</ul>
		
		<div class="main_navi_right">
			<div class="main_navi_right_divs"><g:message code="" default="" />Bank Of Venice</div>
			<div class="main_navi_right_divs"><g:message code="" default="" />megan@venicealehouse.com</div>
			<div class="main_navi_right_divs" style="color:#ffffff;"><g:link controller="admin" action="logout"><g:message code="logout.label" default="Logout" /></g:link></div>
		</div>
		
		<div class="clr"></div>
 	</div>
 
   	<%--<div class="navi_links">
		<ul>
			 <li><g:link controller="admin" action="ordersList"><img src="${resource(dir: 'images', file: 'home-icon.png')}" alt="img" width="20" height="18" /></g:link></li> 
			<li><g:link controller="admin" action="summary"><img src="${resource(dir: 'images', file: 'home-icon.png')}" alt="img" width="20" height="18" /></g:link></li>
			<li><g:link controller="admin" action="saleuserList"><img src="${resource(dir: 'images', file: 'user.png')}" alt="Sales Users" width="14" height="16" /></g:link></li>
            <li><g:link controller="admin" action="usersList"><img src="${resource(dir: 'images', file: 'user.png')}" alt="Users" width="14" height="16" /></g:link></li>
            <li><g:link controller="admin" action="venueList"><img src="${resource(dir: 'images', file: 'venue.png')}" alt="Users" width="14" height="16" /></g:link></li>
            <li><g:link controller="admin" action="appSettings"><img src="${resource(dir: 'images', file: 'settings.png')}" alt="img" width="20" height="18" /></g:link></li>
			<li><g:link controller="admin" action="logout"><img src="${resource(dir: 'images', file: 'logout.png')}" alt="img" width="17" height="17" /></g:link></li>
		</ul>
	</div>
  --%></g:if>
  
  <div class="body-con">
   <g:layoutBody/>
  </div>
	<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
	<div class="footer-con">
   	<div class="footer-copytxt">&copy; <g:message code="page.tool.footer.title" default="2013 Vendsy, Inc. All rights reserved" /></div>
    </div>
  </div>
  </div>
  <g:javascript library="application"/>
    <r:layoutResources />
  </body>
  </html>