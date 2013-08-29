package bartsy

import grails.converters.JSON

class UserRewardsController {

	def userService
	def index() {
	}
	/**
	 * To get the reward points details of the user
	 */
	def getUserRewards={
		def response=[:]
		CommonMethods common = new CommonMethods()
		try{
			def json = JSON.parse(request)
			if(json){
				if(json.has("apiVersion")){
					def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
					if(apiVersion.value.toInteger() == json.apiVersion.toInteger()){
						if(json.has("bartsyId")){
							boolean bartsyId = common.verifyBartsyId(json.bartsyId)
							if(bartsyId){
								def user = UserProfile.findByBartsyId(json.bartsyId)
								if(user.emailVerified.toString().equalsIgnoreCase("true")){
									response=userService.getuserRewards(json.bartsyId,response)
								}else{
									response.put("errorCode","99")
									response.put("errorMessage","Please verify your account to start collecting rewards")
								}
							}
						}
						else{
							response.put("errorCode","1")
							response.put("errorMessage","BartsyId should not be empty or null")
						}
					}else {
						response.put("errorCode","100")
						response.put("errorMessage","API version do not match")
					}
				}else {
					response.put("errorCode","1")
					response.put("errorMessage","API version should not empty")
				}
			}
			else{
				response.put("errorCode", 1)
				response.put("errorMessage", "Invalid request")
			}
		}catch(Exception e){
			log.info("Exception found in getUserRewards "+e.getMessage())
			common.exceptionFound(e,response)
		}
		response.put("currentTime",new Date().toGMTString())
		render(text:response as JSON,contentType:"application/json")
	}
	/**
	 *  This is the method used to get the fb users in the venue
	 */
	def getFaceBookUsers={
		def response=[:]
		CommonMethods common = new CommonMethods()
		try{
			def json = JSON.parse(request)
			println "json "+json
			if(json){
				if(json.has("apiVersion")){
					def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
					if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
						if(json.has("bartsyId") && json.has("venueId")){

							def venue = Venue.findByVenueId(json.venueId)
							if(venue){
								boolean bartsyId = common.verifyBartsyId(json.bartsyId)
								if(bartsyId){
									def fbUsers=[]
									def checkedInUsers = CheckedInUsers.findAllByVenue(venue)
									checkedInUsers.each {
										def checkedInUser = it
										if(checkedInUser.userProfile.facebookId){
											fbUsers.add(checkedInUser.userProfile.bartsyId)
										}
										if(fbUsers.size()>0){
											response.put("errorCode","0")
											response.put("errorMessage","Facebook users available")
											response.put("FbUsers",fbUsers)
											response.put("currentTime",new Date().toGMTString())
										}else{
											response.put("errorCode","5")
											response.put("errorMessage","Facebook users not available in this venue")
										}
									}
								}else{
									response.put("errorCode","4")
									response.put("errorMessage","User does not exists")
								}
							}else{
								response.put("errorCode","3")
								response.put("errorMessage","Venue does not exists")
							}
						}
						else{
							response.put("errorCode","2")
							response.put("errorMessage","BartsyId or VenueId should not be empty or null")
						}
					}else {
						response.put("errorCode","100")
						response.put("errorMessage","API version do not match")
					}
				}else {
					response.put("errorCode","1")
					response.put("errorMessage","API version should not empty")
				}
			}else{
				response.put("errorCode",200)
				response.put("errorMessage","Error occured while processing your request. Please verify json")
			}
		}catch(Exception e){
			println"Exception found in getfb users "+e.getMessage()
			response.put("errorCode",200)
			response.put("errorMessage","Error occured while processing your request. Please verify json")
		}
		render(text:response as JSON,contentType:"application/json")
	}

	/**
	 *  To get getVenueRewards
	 */

	def getVenueRewards={
		def response=[:]
		CommonMethods common = new CommonMethods()
		try{
			def json = JSON.parse(request)
			println"json "+json
			if(json){
				if(json.has("apiVersion")){
					def apiVersion = BartsyConfiguration.findByConfigName("apiVersion")
					if(apiVersion.value.toString().equalsIgnoreCase(json.apiVersion.toString())){
						if(json.has("venueId")){

							def venue = Venue.findByVenueId(json.venueId)
							if(venue){
								response = userService.getVenueRewards(venue)
								response.put("venueId",venue.venueId)
								if(json.bartsyId){
									boolean bartsyId = common.verifyBartsyId(json.bartsyId)
									if(bartsyId){
										def user = UserProfile.findByBartsyId(json.bartsyId)
										if(user.emailVerified.toString().equalsIgnoreCase("true")){
											def rewardsDetails = UserRewardPoints.createCriteria().list() {
												eq("user", user)
												and{
												eq("venue",venue)
												}
												projections {
													sum("rewardPoints")
												}
											}
											println"rewardsDetails "+rewardsDetails
											if(rewardsDetails){
												response.put("userAvailableRewards",rewardsDetails[0])
											}else{
												response.put("rewardPoints", 0)
											}
										}else{
											response.put("rewardPoints", 0)
										}
									}
								}else{
									response.put("rewardPoints", 0)
								}
							}else {
								response.put("errorCode","4")
								response.put("errorMessage","Venue doesn't exist")
							}
						}
						else{
							response.put("errorCode","3")
							response.put("errorMessage","VenueId should not be empty or null")
						}
					}else {
						response.put("errorCode","100")
						response.put("errorMessage","API version do not match")
					}
				}else {
					response.put("errorCode","2")
					response.put("errorMessage","API version should not empty")
				}
			}
			else{
				response.put("errorCode", 1)
				response.put("errorMessage", "Invalid request")
			}
		}catch(Exception e){
			log.info("Exception found in getVenueRewards "+e.getMessage())
			println"Exception found in getVenueRewards "+e.getMessage()
			common.exceptionFound(e,response)
		}
		response.put("currentTime",new Date().toGMTString())
		render(text:response as JSON,contentType:"application/json")
	}
}
