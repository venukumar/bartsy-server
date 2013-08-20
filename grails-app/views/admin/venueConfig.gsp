<%@page import="bartsy.Venue"%>
<!doctype html>
<html>
<head>
<script type="text/javascript">
	/*
	jQuery(document).ready(function() {
	console.log("READY", jQuery("#frm"));
	jQuery(".frm").validate({	
	rules: {
	rewardPoints:{
	required: true, 
	digits:true
	}
	},
	messages:{
	rewardPoints:{
	required:"enter your mobile num", 
	digits:"enter only digits"
	}
	} 
	});
	
	
	});*/
	jQuery(document).ready(function() {
		console.log("READY");
	});
	function validateForm() {
		var rewardPoints = jQuery(".rewards").val();
		if (rewardPoints != "") {
			if (isNaN(rewardPoints) || rewardPoints <= 0) {
				alert("Reward Points should be number");
				return false;
			} else {
				var description = jQuery(".description").val();
				if (description != null && description != "") {
					var value = jQuery(".value").val();
					if (value != null && value != "") {

						if (isNaN(value) || value <= 0) {
							alert("value should be number");
							return false;
						}
						else{return true}
					} else {
						alert("Value should not be empty");
						return false;
					}

				} else {
					alert("Descriptions should not be empty");
					return false;
				}
			}
		} else {
			alert("No value in Reward Points field");
			return false;
		}

	}
</script>
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
	<%--  <div>
		<div>
			<label class="venueConfigstatic">Venue Id :</label>
			${venue.venueId}
		</div>
		<div>
			<label class="venueConfigstatic">Venue Name :</label>
			${venue.venueName}
		</div>

		<g:form action="saveVenueConfig" method="post"
			onSubmit="return validateForm()">

			<input type="hidden" name="venue" value='${venue.venueId}'>
			<div class='venu-con'>
				<div id="userTimeout" class="label-row">
					<label class="venueConfigstatic">Reward Points :</label><input
						type="text" name="rewardPoints" class="txt-field rewards">
				</div>
				<div id="userTimeout" class="label-row">
					<label class="venueConfigstatic">Description :</label>
					<textarea name="description" class="txt-field description" rows="3"
						cols="5"></textarea>
				</div>
				<div id="userTimeout" class="label-row">
					<label class="venueConfigstatic">Types :</label> <select
						class="txt-field pick" name="type">
						<option>Percentage</option>
						<option>Cash</option>
					</select>
				</div>
				<div id="userTimeout" class="label-row">
					<label class="venueConfigstatic">Value :</label> <input type="text"
						name="value" class="txt-field value"></input>
				</div>

				<div id="userTimeout" class="label-row">
					<g:submitButton name="create" class="ybtn" value="Add" />
				</div>
			</div>
		</g:form>

		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			class="tbl-data" padding-top="10px">
			<tr>
				<td>Reward Points</td>
				<td>Description</td>
				<td>Reedem Types</td>
				<td>Action</td>
			</tr>
			<tr>
				<td><input class="venueConfigText" type="text" /></td>
				<td><textarea rows="1" cols="3" class="textAreaVenueConfig"></textarea></td>
				<td><select class="venueConfigText">
						<option>Percentage</option>
						<option>Cash</option>
				</select></td>
				<td><g:submitButton name="create" type="button" value="Add"
						class="venueConfigText" action="saveVenueConfig" /></td>
			</tr>
		</table>

		
		<div>
			<table width="100%" border="0" cellspacing="0" cellpadding="0"
				class="tbl-data">
				<tr>
					
					<th width="20%">Reward Points</th>
					<th width="20%">Value</th>
					<th width="20%">Reedem Type</th>
					<th width="20%">Description</th>
				</tr>
				<% if(configListSize>0){%>
      <g:each in="${venueList}" status="i" var="configList">
	    <tr>
		<td><label>${configList.rewardPoints}</label></td>
		<td><label>${configList.value}</label></td>
		<td><label>${configList.type}</label></td>
		<td><label>${configList.description}</label></td>
	  </tr>
      </g:each>
      <% }else { %>
        <tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      <% } %>
			</table>
		</div>


		<%--<table width="100%" border="0" cellspacing="0" cellpadding="0"
			class="tbl-data">
			<tr>
				<th width="20%"><g:message code="venue.id.label"
						default="Venue Id" /></th>
				<th width="20%"><g:message code="venue.name.label"
						default="Venue Name" /></th>
			</tr>
			<tr>
				<td><div
						style="width: 250px; word-wrap: break-word; display: block;">
						${venue.venueId}
					</div></td>
				<td>
					${venue.venueName}
				</td>

			</tr>
		</table>
	
	</div>--%>
	
<div class="main_container">
<g:form action="saveVenueConfig" method="post">
	<input type="hidden" name="venue" value='${venue.venueId}'>
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
		
		<div class="right_con_leftpart">
			<div><g:img dir="images" file="apple-touch-icon-retina.png" /></div>
			<div><g:submitButton name="Edit Image" value="Edit Image" class="edit_btn" /></div>
		</div>
		
		<div class="right_con_rightpart">
				
			<div class="margin_btm">
				<div class="left"><g:message code="name.label" default="Name"/> : </div>
				<div class="left" style="margin-left:30px;"><input type="text" name="" class="txt-field rewards" ></div>
				<div class="clr"></div>
			</div>
				
			<div class="margin_btm">
				<div class="left"><g:message code="address.label" default="Address"/> :</div>
				<div class="left" style="margin-left:18px;"><textarea></textarea></div>
				<div class="clr"></div>
			</div>
				
			<div class="margin_btm">
				<div class="left"><g:message code="hours.label" default="Hours"/> : </div>
				<div class="left" style="margin-left:30px;"> M &nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 T &nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/>	<br/>			
				 W &nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 T &nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> <br/>
				 F &nbsp&nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				 S &nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> <br/>
				 S &nbsp&nbsp<input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/> to <input type="text" class="hrs_text_field"/> : <input type="text" class="hrs_text_field"/>
				</div>
				<div class="clr"></div>
			</div>
				 	
			<div><g:submitButton name="update" type="button" value="Update" class="update_btn" action="saveVenueConfig" /></div>
				 				
		</div>
		<div class="clr"></div>
		
	</div>
	<div class="clr"></div>
</g:form>
</div>
</body>
</html>