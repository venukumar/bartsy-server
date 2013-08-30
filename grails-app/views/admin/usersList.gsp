<%@page import="bartsy.*"%>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.user.list.title" default="Users List" /></title>
  </head>
  <body>
  <h2>Users List</h2>
  <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>	<% flash.clear() %>
    <div>
		<table class="tbl-data">
			<tr>
				<th width="10%"><g:link controller="admin" action="summary"><g:message code="summary.label" default="Summary" /></g:link></th>
				<th width="10%"><g:link controller="admin" action="categories"><g:message code="categories.label" default="Categories" /></g:link></th>
				<th width="10%"><g:link controller="admin" action="ordersList"><g:message code="items.label" default="Items" /></g:link></th>
				<th width="10%"><g:link controller="admin" action="usersList"><g:message code="guests.label" default="Guests" /></g:link></th>
			</tr>
		</table>
	</div>
	<div>
      <table class="tbl-data">
        <tr>
        <th width="10%"><g:message code="user.id.label" default="Bartsy Id" /></th>
		<th width="15%"><g:message code="user.name.label" default="Name" /></th>
		<th width="25%"><g:message code="user.email.label" default="Email" /></th>
		<th width="10%"><g:message code="user.age.label" default="Age" /></th>
		<th width="20%"><g:message code="user.checked.venues" default="Checked In Venues" /></th>
		<th width="20%"><g:message code="user.lastorderdate.label" default="Last Ordered Date" /></th>
		<th width="10%"><g:message code="user.total.order" default="Orders" /></th>
		</tr>
		<!-- <modalbox:createLink controller="admin" action="userDetails" id="${userInfo?.bartsyId}" title="Show User Information" width="750"></modalbox:createLink>-->
    <% if(usersTotal>0){%>
      <g:each in="${usersList}" status="i" var="userInfo">
	  <% 
	  	 def checkedInVenues = UserCheckInDetails.findAllByUserProfile(userInfo)*.venue.unique()
		 def venueName=''
		 if(checkedInVenues){
		 	checkedInVenues.each{
				def venueId = it.venueId
				def venue = Venue.findByVenueId(venueId)
				venueName+=venue.venueName+","
			}
			venueName = venueName.substring(0, venueName.length() - 1)
		 }
		  
	  	 def orderList = Orders.createCriteria().list(){
		 	eq("user",userInfo)
			order "id", "desc"
		 }
	  	
	  	 CommonMethods commonMethods = new CommonMethods()
		 def age
		 if(userInfo.getDateOfBirth()){
		 	age= commonMethods.getAge(userInfo.getDateOfBirth())
		 }
	  %>
	    <tr>
          <td>${userInfo.bartsyId}</td>
          <td>
		  	<modalbox:createLink controller="admin" action="userDetails" id="${userInfo?.bartsyId}" title="User Details" width="750">
		  	<% if(userInfo.firstName){%>
			  ${userInfo.firstName} ${userInfo.lastName}
		  	  <% }else{ %>
				${userInfo.nickName}
			<% } %>
		  	</modalbox:createLink>
		  </td>
          <td>
		  	<modalbox:createLink controller="admin" action="userDetails" id="${userInfo?.bartsyId}" title="User Details" width="750">
		  	${userInfo.email}
			</modalbox:createLink>
		  </td>
	  	  <td>${age?:''}</td>
	  	  <td>${venueName}</td>
		  <td>${orderList[0]?.dateCreated?.format('hh:mm a, MMM dd,yyyy')}</td>
	  	  <td>${orderList.size()}</td>
        </tr>
      </g:each>
      <% }else { %>
        <tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      <% } %>
      </table>
    <% if(usersTotal>50){%>
      <div class="pagination">
       <g:paginate action="usersList" total="${usersTotal}" />
      </div>
      <% } %>
    </div>
  </body>
  </html>