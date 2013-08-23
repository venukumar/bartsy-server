<%@page import="bartsy.Venue"%>
<%@page import="org.codehaus.groovy.grails.web.json.JSONObject" %>
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
	<div class="left_container">
		<ul>
			<li class="left_panel"><g:link action="venueConfig" params="${[vc:1]}" id="${venue?.venueId}"><g:message code="how.users.will.see.your.venue" default="How users will see your venue" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[mgr:2]}" id="${venue?.venueId}"><g:message code="vendsy.manager.label" default="Manager" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[vRep:3]}" id="${venue?.venueId}"><g:message code="vendsy.representative.label" default="Vendsy representative" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[menu:4]}" id="${venue?.venueId}"><g:message code="menu.label" default="Menu" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[orders:5]}" id="${venue?.venueId}"><g:message code="venue.orders.label" default="Orders" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[bankAcct:6]}" id="${venue?.venueId}"><g:message code="bank.account.label" default="Bank account" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" params="${[wifi:7]}" id="${venue?.venueId}"><g:message code="wifi.label" default="WiFi" /></g:link></li>
  		</ul>
	</div>
<g:form action="saveVenueConfig" method="post">
	<input type="hidden" name="venueId" value='${venue?.venueId}'>
		<!-- How users will see your venue -->
	<%if (params.vc){ %>
		<input type="hidden" name="vc" value='${params.vc}'>
		<div class="right_container">
		<div class="right_con_leftpart">
			<div><g:img dir="/Bartsy/venueImages" file="100001" /></div>
			<div><g:submitButton name="Edit Image" value="Edit Image" class="edit_btn" /></div>
		</div>
		<div class="right_con_rightpart">
				
			<div class="margin_btm">
				<div class="left"><g:message code="name.label" default="Name"/> : </div>
				<div class="left" style="margin-left:30px;"><input type="text" name="venueName" value="${venue?.venueName}" class="txt-field rewards" ></div>
				<div class="clr"></div>
			</div>
				
			<div class="margin_btm">
				<div class="left"><g:message code="address.label" default="Address"/> :</div>
				<div class="left" style="margin-left:18px;"><textarea name="address" >${venue?.address}</textarea></div>
				<div class="clr"></div>
			</div>
			<%
				String[] monFrom = mon[0].split(':')
				String[] monTo = mon[1].split(':')
				String[] tuesFrom = tues[0].split(':')
				String[] tuesTo = tues[1].split(':')
				String[] wedFrom = wed[0].split(':')
				String[] wedTo = wed[1].split(':')
				String[] thursFrom = thurs[0].split(':')
				String[] thursTo = thurs[1].split(':')
				String[] friFrom = fri[0].split(':')
				String[] friTo = fri[1].split(':')
				String[] satFrom = sat[0].split(':')
				String[] satTo = sat[1].split(':')
				String[] sunFrom = sun[0].split(':')
				String[] sunTo = sun[1].split(':')
			 %>	
			<div class="margin_btm">
				<div class="left"><g:message code="hours.label" default="Hours"/> : </div>
				<div class="left" style="margin-left:30px;"> M &nbsp<input type="text" name="monFromHrs"  value="${monFrom[0]}" class="hrs_text_field"/> : <input type="text" name="monFromMins"  value="${monFrom[1]}" class="hrs_text_field"/> to <input type="text" name="monToHrs"  value="${monTo[0]}" class="hrs_text_field"/> : <input type="text" name="monToMins"  value="${monTo[1]}" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 T &nbsp<input type="text" name="tuesFromHrs" value="${tuesFrom[0]}" class="hrs_text_field"/> : <input type="text" name="tuesFromMins" value="${tuesFrom[1]}" class="hrs_text_field"/> to <input type="text" name="tuesToHrs" value="${tuesTo[0]}" class="hrs_text_field"/> : <input type="text" name="tuesToMins" value="${tuesTo[1]}" class="hrs_text_field"/>	<br/>			
				 W &nbsp<input type="text" name="wedFromHrs" value="${wedFrom[0]}" class="hrs_text_field"/> : <input type="text" name="wedFromMins" value="${wedFrom[1]}" class="hrs_text_field"/> to <input type="text" name="wedToHrs" value="${wedTo[0]}" class="hrs_text_field"/> : <input type="text" name="wedToMins" value="${wedTo[1]}" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 T &nbsp<input type="text" name="thursFromHrs" value="${thursFrom[0]}" class="hrs_text_field"/> : <input type="text" name="thursFromMins" value="${thursFrom[1]}" class="hrs_text_field"/> to <input type="text" name="thursToHrs" value="${thursTo[0]}" class="hrs_text_field"/> : <input type="text" name="thursToMins" value="${thursTo[1]}" class="hrs_text_field"/> <br/>
				 F &nbsp&nbsp<input type="text" name="friFromHrs" value="${friFrom[0]}" class="hrs_text_field"/> : <input type="text" name="friFromMins" value="${friFrom[1]}" class="hrs_text_field"/> to <input type="text" name="friToHrs" value="${friTo[0]}" class="hrs_text_field"/> : <input type="text" name="friToMins" value="${friTo[1]}" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 S &nbsp<input type="text" name="satFromHrs" value="${satFrom[0]}" class="hrs_text_field"/> : <input type="text" name="satFromMins" value="${satFrom[1]}" class="hrs_text_field"/> to <input type="text" name="satToHrs" value="${satTo[0]}" class="hrs_text_field"/> : <input type="text" name="satToMins" value="${satTo[1]}" class="hrs_text_field"/> <br/>
				 S &nbsp&nbsp<input type="text" name="sunFromHrs" value="${sunFrom[0]}" class="hrs_text_field"/> : <input type="text" name="sunFromMins" value="${sunFrom[1]}" class="hrs_text_field"/> to <input type="text" name="sunToHrs" value="${sunTo[0]}" class="hrs_text_field"/> : <input type="text" name="sunToMins" value="${sunTo[1]}" class="hrs_text_field"/>
				</div>
				<div class="clr"></div>
			</div>
				 	
			<div>
				<g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="update_btn" />
			</div>
				 				
		</div>
		<div class="clr"></div>
		
	</div>
	<%}%>
	
	<!-- Manager -->
	<%if (params.mgr){ %>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="name.label" default="Name" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="email.label" default="Username/Email" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="password.label" default="Password" /> : </td>
				<td><input type="password" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="confirmpwd.label" default="Confirm" /> : </td>
				<td><input type="password" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="phone.label" default="Cell" /> : </td>
				<td><input type="text" name="" class="txt-field rewards" placeholder="Your cell phone for emergencies"></td>
			</tr>
				
			<tr>
				<td colspan="2">
					<g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" />
				</td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!--  Vendsy representative -->
	<%if (params.vRep) {%>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="name.label" default="Name" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="email.label" default="Username/Email" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="phone.label" default="Cell" /> : </td>
				<td><input type="text" name="" class="txt-field rewards" ></td>
			</tr>
				
			<tr>
				<td colspan="2"><g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!--  Menu -->
	<%if (params.menu){ %>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="locuusername.label" default="Locu username" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="locupwd.label" default="Locu password" /> : </td>
				<td><input type="password" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="locuid.label" default="Locu ID" /> : </td>
				<td><input type="text" name="" class="txt-field rewards" ></td>
			</tr>
			<tr>
				<td><g:message code="locusection.label" default="Locu section" /> : </td>
				<td><input type="text" name="" class="txt-field rewards" ></td>
			</tr>
			
			<tr>
				<td colspan="2"><g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!-- Orders -->
	<%if (params.orders) {%>
		<div class="right_container">
		<div class="left"><g:message code="ordertimeout.label" default="Order timeout"/> : <input type="text" name="" class="hrs_text_field " > minutes </div>
		<div class="left">&nbsp;&nbsp;&nbsp;&nbsp;<g:message code="totaltaxrate.label" default="Total tax rate"/> : <input type="text" name="" class="hrs_text_field " > % </div>
		<div class="clr"></div>
		<div><g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="update_btn" action="saveVenueConfig" /></div>
	</div>
	<%}%>
	
	<!--  Bank account -->
	<%if (params.bankAcct){ %>
		<div class="right_container">
		<table>
			<tr>
				<td><g:message code="routingnumber.label" default="Routing Number" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="accountnumber.label" default="Account Number" /> : </td>
				<td><input type="text" name="" class="txt-field rewards" ></td>
			</tr>
				
			<tr>
				<td colspan="2"><g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table>
		
	</div>
	<%}%>
	
	<!-- Wifi -->
	<%if (params.wifi){ %>
		<div class="right_container">
		<table>
			<tr>
				<td><g:message code="wifipresent.label" default="Wifi Present" /> : </td>
				<td><g:checkBox name="" value="" /></td>
			</tr>
			<tr>
				<td><g:message code="wifiname.label" default="Wifi Name" /> : </td>
				<td><input type="text" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="wificode.label" default="Wifi Code" /> : </td>
				<td><input type="password" name="" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="authentication.label" default="Authentication" /> : </td>
				<td>
					<input type="radio" name="">&nbsp;<g:message code="password.label" default="Password" />
					<input type="radio" name="">&nbsp;<g:message code="passphrase.label" default="Passphrase" />
				</td>
				<td></td>
			</tr>
			<tr>
				<td><g:message code="networktype.label" default="Network Type" /> : </td>
				<td>
					<input type="radio" name="">&nbsp;<g:message code="wpa.label" default="WPA" />
					<input type="radio" name="">&nbsp;<g:message code="wep.label" default="WEP" />
				</td>
				<td></td>
			</tr>
				
			<tr>
				<td colspan="2"><g:submitButton name="update" type="button" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table>
		
	</div>
	<%}%>
	<div class="clr"></div>
</g:form>
</div>
</body>
</html>