<%@page import="bartsy.Venue"%>
<!doctype html>
<html>
<head>

<style type="text/css">
label.error {
	display:;
	color: red;
	margin-left: 0px;
}
</style>
<meta name="layout" content="main">
	<title><g:message code="page.venue.list.title" default="Venue List" /></title>
</head>
<body>
	<h2>Venue Configuration</h2>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	
<div class="main_container">
<g:form action="saveVenueConfig" method="post">
			<%-- <input type="hidden" name="venue" value='${venue.venueId}'>--%>
		
	<div class="left_container">
		<ul>
			<li class="left_panel"><g:link action="venueConfig" id="${venue.venueId}"><g:message code="how.users.will.see.your.venue" default="How users will see your venue" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigManager" id="${venue.venueId}"><g:message code="vendsy.manager.label" default="Manager" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigVendsyRep" id="${venue.venueId}"><g:message code="vendsy.representative.label" default="Vendsy representative" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigMenu" id="${venue.venueId}"><g:message code="menu.label" default="Menu" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigOrders" id="${venue.venueId}"><g:message code="venue.orders.label" default="Orders" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigBankAccount" id="${venue.venueId}"><g:message code="bank.account.label" default="Bank account" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfigWifi" id="${venue.venueId}"><g:message code="wifi.label" default="WiFi" /></g:link></li>
  		</ul>
	</div>

	<div class="right_container">
		<div class="left"><g:message code="ordertimeout.label" default="Order timeout"/> : <input type="text" name="" class="hrs_text_field " > minutes </div>
		<div class="left">&nbsp;&nbsp;&nbsp;&nbsp;<g:message code="totaltaxrate.label" default="Total tax rate"/> : <input type="text" name="" class="hrs_text_field " > % </div>
		<div class="clr"></div>
		<div><g:submitButton name="update" type="button" value="Update" class="update_btn" action="saveVenueConfig" /></div>
	</div>
	
	<div class="clr"></div>
</g:form>
</div>
</body>
</html>