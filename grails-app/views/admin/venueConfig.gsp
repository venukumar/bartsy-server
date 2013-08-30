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

	<div style="border-bottom:1px solid #e7e7e7;">
		<div class="left"><h2>Venue Configuration</h2></div>
		<div class="right"><h2><g:message code="venue.name.label" default="Venue Name" /> : ${venue?.venueName}</h2></div>
		<div class="clr"></div>
	</div>
	
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
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[mgr:2]}" id="${venue?.venueId}"><g:message code="vendsy.manager.label" default="Manager" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[vRep:3]}" id="${venue?.venueId}"><g:message code="vendsy.representative.label" default="Vendsy representative" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[menu:4]}" id="${venue?.venueId}"><g:message code="menu.label" default="Menu" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[orders:5]}" id="${venue?.venueId}"><g:message code="venue.orders.label" default="Orders" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[bankAcct:6]}" id="${venue?.venueId}"><g:message code="bank.account.label" default="Bank account" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[wifi:7]}" id="${venue?.venueId}"><g:message code="wifi.label" default="WiFi" /></g:link></li>
    		<li class="left_panel"><g:link action="venueConfig" onclick="${venue?'return true':'return false'}" params="${[rew:8]}" id="${venue?.venueId}"><g:message code="rewards.label" default="Rewards" /></g:link></li>
  		</ul>
	</div>
<g:form action="saveVenueConfig" method="post">
	<input type="hidden" name="venueId" value='${venue?.venueId}'>
		<!-- How users will see your venue -->
	<%if (params.vc){ %>
		<input type="hidden" name="vc" value='${params.vc}'>
		<div class="right_container">
		<div class="right_con_leftpart">
			<div><g:img dir="Bartsy/venueImages" file="100001.JPG" /></div>
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
		<input type="hidden" name="mgr" value='${params.mgr}'>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="name.label" default="Name" /> : </td>
				<td><input type="text" name="mgrName" value="${venue?.managerName}" class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="email.label" default="Username/Email" /> : </td>
				<td><input type="text" name="mgrEmail" value="${venue?.venueLogin}" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="password.label" default="Password" /> : </td>
				<td><input type="password" name="mgrPassword" value="${venue?.venuePassword}" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="confirmpwd.label" default="Confirm" /> : </td>
				<td><input type="password" name="mgrConfirm" value="${venue?.venuePassword}" class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="phone.label" default="Cell" /> : </td>
				<td><input type="text" name="mgrCell" value="${venue?.phoneNumber}" class="txt-field rewards" placeholder="Your cell phone for emergencies"></td>
			</tr>
				
			<tr>
				<td colspan="2">
					<g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" />
				</td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!--  Vendsy representative -->
	<%if (params.vRep) {%>
	<input type="hidden" name="vRep" value='${params.vRep}'>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="name.label" default="Name" /> : </td>
				<td><input type="text" name="vendsyRepName" value='${(venue?.vendsyRepName)}' class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="email.label" default="Username/Email" /> : </td>
				<td><input type="text" name="vendsyRepEmail" value='${(venue?.vendsyRepEmail)}' class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="phone.label" default="Cell" /> : </td>
				<td><input type="text" name="vendsyRepPhone" value='${(venue?.vendsyRepPhone)}' class="txt-field rewards" ></td>
			</tr>
				
			<tr>
				<td colspan="2"><g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!--  Menu -->
	<%if (params.menu){ %>
	<input type="hidden" name="menu" value='${params.menu}'>
	<div class="right_container">
		<table>
			<tr>
				<td><g:message code="locuusername.label" default="Locu username" /> : </td>
				<td><input type="text" name="locuUsername" value='${(venue?.locuUsername)}' class="txt-field rewards"></td>
			</tr>
			<tr>	
				<td><g:message code="locupwd.label" default="Locu password" /> : </td>
				<td><input type="password" name="locuPassword" value='${(venue?.locuPassword)}' class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="locuid.label" default="Locu ID" /> : </td>
				<td><input type="text" name="locuId" value='${(venue?.locuId)}' class="txt-field rewards" ></td>
			</tr>
			<tr>
				<td><g:message code="locusection.label" default="Locu section" /> : </td>
				<td><input type="text" name="locuSection" value='${(venue?.locuSection)}' class="txt-field rewards" ></td>
			</tr>
			
			<tr>
				<td colspan="2"><g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table> 
		
	</div>
	<%}%>
	
	<!-- Orders -->
	<%if (params.orders) {%>
		<input type="hidden" name="orders" value='${params.orders}'>
		<div class="right_container">
		<div class="left"><g:message code="ordertimeout.label" default="Order timeout"/> : <input type="text" name="cancelOrderTime" value='${(venue?.cancelOrderTime)}' class="hrs_text_field " > minutes </div>
		<div class="left">&nbsp;&nbsp;&nbsp;&nbsp;<g:message code="totaltaxrate.label" default="Total tax rate"/> : <input type="text" name="totalTaxRate" value='${(venue?.totalTaxRate)}' class="hrs_text_field " > % </div>
		<div class="clr"></div>
		<div><g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="update_btn" action="saveVenueConfig" /></div>
	</div>
	<%}%>
	
	<!--  Bank account -->
	<%if (params.bankAcct){ %>
		<input type="hidden" name="bankAcct" value='${params.bankAcct}'>
		<div class="right_container">
		<table>
			<tr>
				<td><g:message code="routingnumber.label" default="Routing Number" /> : </td>
				<td><input type="text" name="routingNumber" value='${(venue?.routingNumber)}' class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="accountnumber.label" default="Account Number" /> : </td>
				<td><input type="text" name="accountNumber" value='${(venue?.accountNumber)}' class="txt-field rewards" ></td>
			</tr>
			<tr>
				<td colspan="2"><g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table>
		
	</div>
	<%}%>
	
	<!-- Wifi -->
	<%if (params.wifi){ %>
		<input type="hidden" name="wifi" value='${params.wifi}'>
		<div class="right_container">
		<table>
			<tr>
				<td><g:message code="wifipresent.label" default="Wifi Present" /> : </td>
				<!--<td><g:checkBox name="wifiPresent" checked='${(venue?(venue.wifiPresent == 1?true:false):false)}' /></td>-->
				<td><input type='checkBox' name="wifiPresent" ${(venue?(venue.wifiPresent == 1?'checked=true':''):'')} /></td>
			</tr>
			<tr>
				<td><g:message code="wifiname.label" default="Wifi Name" /> : </td>
				<td><input type="text" name="wifiName" value='${(venue?.wifiName)}' class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="wificode.label" default="Wifi Code" /> : </td>
				<td><input type="password" name="wifiPassword" value='${(venue?.wifiPassword)}' class="txt-field rewards"></td>
			</tr>
			<tr>
				<td><g:message code="authentication.label" default="Authentication" /> : </td>
				<td>
					<input type="radio" name="authType" value="Password" ${(venue?(venue.typeOfAuthentication.equalsIgnoreCase('Password')?'checked=true':''):'')}>&nbsp;<g:message code="password.label" default="Password" />
					<input type="radio" name="authType" value="Passphrase" ${(venue?(venue.typeOfAuthentication.equalsIgnoreCase('Passphrase')?'checked=true':''):'')}>&nbsp;<g:message code="passphrase.label" default="Passphrase" />
				</td>
				<td></td>
			</tr>
			<tr>
				<td><g:message code="networktype.label" default="Network Type" /> : </td>
				<td>
					<input type="radio" name="networkType" value="WPA" ${(venue?(venue.wifiNetworkType.equalsIgnoreCase('WPA')?'checked=true':''):'')}>&nbsp;<g:message code="wpa.label" default="WPA" />
					<input type="radio" name="networkType" value="WEP" ${(venue?(venue.wifiNetworkType.equalsIgnoreCase('WEP')?'checked=true':''):'')}>&nbsp;<g:message code="wep.label" default="WEP" />
				</td>
				<td></td>
			</tr>
				
			<tr>
				<td colspan="2"><g:submitButton name="update" type="submit" value='${(venue?"Update":"Save")}' class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table>
		
	</div>
	<%}%>
	
	<!-- Rewards -->
	<%if (params.rew){ %>
		<input type="hidden" name="rew" value='${params.rew}'>
		<div class="right_container">
			<div>
				<div class="right">
					<div id="userTimeout" class="label-row" style="float:left;padding:3px;">
			  			<div class="btns">
				 			<modalbox:createLink controller="admin" action="addEditVenueReward" params="${[venueId:venue.venueId]}" title="Add Reward" width="750">
					 			<g:submitButton name="update" type="submit" value='Add Reward' class="general_btn" />
				 			</modalbox:createLink>
			 			</div>
			 			<div class="btns"><img src="${resource(dir: 'images', file: 'icon.png')}" class="tooltip imagToolTip"
				 			title="Dollar discounts are automatically applied to orders as long as the user has the number of points specified, or more. General rewards describe explicitly what the reward is for. They show as dialogs on the patron's mobile phone. A staff member can simply click on those rewards to mark them used. When a discount is applied, the user point balance is adjusted automatically by subtracting the points used." />
			 			</div>
			 			<div class="clr"></div>
		   			</div>
		   			<div class="clr"></div>
	  			</div>
	  			<div class="clr"></div>
			</div>
		<table class="tbl-data">
			<tr>
				<th width="20%"><g:message code="reward.points.label" default="Reward Points"/></th>
				<th width="20%"><g:message code="value.label" default="Value"/></th>
				<th width="20%"><g:message code="reward.type.label" default="Reward Type"/></th>
				<th width="20%"><g:message code="description.label" default="Description"/></th>
				<th width="20%" colspan="2"><g:message code="page.list.actions" default="Actions" /></th>
			</tr>
			<% if(configListSize>0){%>
				  <g:each in="${venueList}" status="i" var="configList">
					<tr>
						<td><label>${configList.rewardPoints}</label></td>
						<td><label>${configList.value}</label></td>
						<td><label>${configList.type}</label></td>
						<td><label>${configList.description}</label></td>
						<td>
							<modalbox:createLink controller="admin" action="addEditVenueReward" params="${[venueId:venue.venueId]}" id="${configList.id}" title="Edit reward" width="750">
								<label class="edit"><g:message code="editbutton.title" default="Edit" /></label>
							</modalbox:createLink>
						</td>
						<td><g:link class="delete" action="deleteVenueReward" params="${[venueId:venue.venueId]}" id="${configList.id}" onclick="return confirm('${message(code: 'default.button.delete.sender.message', default: 'Are you sure you wish to delete this reward?')}');"><g:message code="deletebutton.title" default="Delete" /></g:link></td>
					</tr>
				  </g:each>
			  <% }else { %>
				<tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
			  <% } %>
		</table>
		<% if(configListSize>10){%>
		  <div class="pagination">
			<g:paginate total="${configListSize}" />
		  </div>
		<% } %>
		</div>
	<%}%>
	<div class="clr"></div>
</g:form>
</div>
</body>
</html>