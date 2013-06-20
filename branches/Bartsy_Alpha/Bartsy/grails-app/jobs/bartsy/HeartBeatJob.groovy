package bartsy



class HeartBeatJob {
	
	def timeoutService
	
    static triggers = {
      cron name: 'HeartBeat', cronExpression: "0 0/2 * * * ?"
    }

    def execute() {
        timeoutService.heartBeat()
    }
}
