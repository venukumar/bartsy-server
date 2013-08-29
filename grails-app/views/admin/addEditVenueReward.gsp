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
				<div id="userTimeout" class="label-row">
					<label><g:message code="reward.type.label" default="Reward Type"/> :</label> 
					<select class="txt-field pick" name="type">
						<option value="1" ${(venueReward?(venueReward.type.equals("Discount")?'selected=selected':''):'')}><g:message code="discount.label" default="Discount"/></option>
						<option value="2" ${(venueReward?(venueReward.type.equals("General")?'selected=selected':''):'')}><g:message code="general.label" default="General"/></option>
					</select>
				</div>
				<div id="userTimeout" class="label-row reVal">
					<label><g:message code="value.label" default="Value"/> :</label> 
					<input type="text" name="value" value="${(venueReward?.value)}" class="txt-field value"></input>
				</div>
				<div id="userTimeout" class="label-row">
					<label><g:message code="reward.points.label" default="Reward Points"/> :</label>
					<input type="text" name="rewardPoints" value="${(venueReward?.rewardPoints)}" class="txt-field rewards">
				</div>
				<div id="userTimeout" class="label-row desc">
					<label><g:message code="description.label" default="Description"/> :</label>
					<textarea name="description" class="txt-field description" rows="3"	cols="5">${(venueReward?.description)}</textarea>
				</div>
				<div id="userTimeout" class="label-row">
					<g:submitButton name="create" class="ybtn" value="Save" />
				</div>
			</div>
		</g:form>
	</div>
</body>
</html>