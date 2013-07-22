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
			println" user rewards"
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
		render(text:response as JSON,contentType:"application/json")
	}
}
