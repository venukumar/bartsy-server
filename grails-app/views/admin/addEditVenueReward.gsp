<%@page import="bartsy.Venue"%>
<%@page import="org.codehaus.groovy.grails.web.json.JSONObject" %>
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
		var rewardType = jQuery('.pick').val();
		if (rewardType == 1){
			jQuery('.reVal').show()
		}else if (rewardType == 2){
			jQuery('.reVal').hide()
			jQuery('.desc').show()
		}
	});
	var $j = jQuery.noConflict();
	/*function validateForm() {
		var rewardPoints = jQuery(".rewards").val();
		console.log("Reward points-->"+rewardPoints)
		if (rewardPoints != "" && rewardPoints != null) {
			if (isNaN(rewardPoints) || rewardPoints <= 0) {
				alert("Reward Points should be number");
				return false;
			} else {
				var redeemType = jQuery(".pick").val()
				if (redeemType == 1){
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
				}else{
					var description = jQuery(".description").val();
					if (description == null || description=!= "") {
						alert("Descriptions should not be empty");
						return false;
					} 
				}
			}
		} else {
			alert("No value in Reward Points field");
			return false;
		}
	}*/
	jQuery('.pick').change(function() {
		var rewardType = jQuery('.pick').val();
		if (rewardType == 1){
			jQuery('.reVal').show()
		}else if (rewardType == 2){
			jQuery('.reVal').hide()
			jQuery('.desc').show()
		}
	});
</script>
<style type="text/css">
label.error {
	display:;
	color: red;
	margin-left: 0px;
}
</style>
</head>
<body>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	<div>
		<g:form action="saveVenueReward" method="post" onSubmit="return validateForm()">
			<input type="hidden" name="venueId" value='${venueId}'>
			<input type="hidden" name="venueRewardId" value='${venueReward?.id}'>
			<div class='venu-con'>
				
				<div>
					<div id="userTimeout" class="left">
						<label class="left margin_top"><g:message code="reward.type.label" default="Reward Type" /> :</label> 
						  <div class="right">	
						  	
							<select class="pick" name="type">
								<option value="1" ${(venueReward?(venueReward.type.equals("Discount")?'selected=selected':''):'')}><g:message code="discount.label" default="Discount"/></option>
								<option value="2" ${(venueReward?(venueReward.type.equals("General")?'selected=selected':''):'')}><g:message code="general.label" default="General"/></option>
							</select>
						  </div>
						<div class="clr"></div>
					</div>
				
					<div id="userTimeout" class="reVal left">
						<label><g:message code="value.label" default="Value"/> :</label> 
						<input type="text" name="value" value="${(venueReward?.value)}" class="value input_field"></input>
					</div>
				
					<div class="clr"></div>
				</div>
				
				<div id="userTimeout">
					<label><g:message code="reward.points.label" default="Reward Points"/> :</label>
					<input type="text" name="rewardPoints" value="${(venueReward?.rewardPoints)}" class="rewards input_field">
				</div>
				
				<div id="userTimeout">
					<label class="left margin_top"><g:message code="description.label" default="Description"/> :</label>
					<textarea name="description" class="description" class="right">${(venueReward?.description)}</textarea>
					<div class="clr"></div>
				</div>
				
				<div id="userTimeout" style="padding:10px 0px 0px 105px;">
					<!--<g:submitButton name="create" value="Save" class="btn_bg" />-->
					<!--<g:submitButton name="create" value="Cancel" class="btn_bg" />-->
					<input type="submit" name="create" value="Save" class="btn_bg" />
					<g:link action="venueConfig" class="btn_bg" params="${[rew:8]}" id="${venueId}" style="color: white;"><g:message code="cancelbutton.label" default="Cancel" /></g:link>
				</div>
			</div>
		</g:form>
	</div>
</body>
</html>