package bartsy

import grails.converters.JSON


class UserController {

	def index() {
	}

	def saveUserProfile = {
		def json = JSON.parse(request)
		Map response = new HashMap()
		def userProfile = UserProfile.findByUserName(json.userName)
		if(userProfile) {
			response.put("bartsyUserId",userProfile.barstyId)
			render(text:response as JSON ,  contentType:"application/json")
		}
		else{
			UserProfile userProfileToSave = new UserProfile()
			userProfileToSave.setUserName(json.userName)
			userProfileToSave.setName(json.name)
			userProfileToSave.setLoginId(json.loginId)
			userProfileToSave.setLoginType(json.loginType)
			userProfileToSave.setGender(json.gender)
			def maxId = UserProfile.createCriteria().get {
				projections { max "barstyId" }
			} as Long
			if(maxId){
				maxId = maxId+1
			}
			else{
				maxId = 100001
			}
			userProfileToSave.setBarstyId(maxId)
			response.put("bartsyUserId",maxId)
			userProfileToSave.save()
			render(text:response as JSON ,  contentType:"application/json")
		}
	}
}
