<%@page import="bartsy.Venue"%>
<%@page import="org.codehaus.groovy.grails.web.json.JSONObject" %>
<!doctype html>
<html>
<head>
<meta name="layout" content="main">
<title><g:message code="page.venue.rewards.title" default="Venue Rewards" /></title>
<link href="${resource(dir: 'css', file: 'tooltipster.css')}" rel="stylesheet">
<style type="text/css">
label.error {
	display:;
	color: red;
	margin-left: 0px;
}
</style>
</head>
<body>
	<h2>Rewards</h2>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	<div>
		<div>
			<label><g:message code="venue.name.label" default="Venue Name"/> :</label>
			${venue.venueName}
		</div>
		<div>
			<table width="100%" border="0" cellspacing="0" cellpadding="0" class="tbl-data">
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
								<modalbox:createLink controller="admin" action="addEditVenueReward" params="${[venueId:venue.venueId]}" id="${configList.id}" title="Edit reward" width="750" class="edit">
									<g:message code="editbutton.title" default="Edit" />
								</modalbox:createLink>
							</td>
							<td><g:link class="delete" action="deleteVenueReward" params="${[venueId:venue.venueId]}" id="${configList.id}" onclick="return confirm('${message(code: 'default.button.delete.sender.message', default: 'Are you sure you wish to delete this reward?')}');"><g:message code="deletebutton.title" default="Delete" /></g:link></td>
						</tr>
      				</g:each>
      			<% }else { %>
        			<tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      			<% } %>
			</table>
			<g:message code="add.reward.type.label" default="Add a reward of type:" />
			<div id="userTimeout" class="label-row">
				<modalbox:createLink controller="admin" action="addEditVenueReward" params="${[venueId:venue.venueId, rewardType:"1"]}" title="Dollar discount" width="750">
					${"Dollar discount"}
				</modalbox:createLink>
				<modalbox:createLink controller="admin" action="addEditVenueReward" params="${[venueId:venue.venueId, rewardType:"2"]}" title="General" width="750">
					${"General"}
				</modalbox:createLink>
				<img src="${resource(dir: 'images', file: 'icon.png')}" class="tooltip imagToolTip"
				title="Dollar discounts are automatically applied to orders as long as the user has the number of points specified, or more. General rewards describe explicitly what the reward is for. They show as dialogs on the patron's mobile phone. A staff member can simply click on those rewards to mark them used. When a discount is applied, the user point balance is adjusted automatically by subtracting the points used." />
			</div>
			<% if(configListSize>10){%>
      		<div class="pagination">
        		<g:paginate total="${configListSize}" />
      		</div>
      		<% } %>
		</div>
	</div>
</body>
</html>