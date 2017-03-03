package worker

// ----- Imports ----- //

// For Java Lists
import scala.collection.JavaConversions.asScalaBuffer

// AWS Java SDK
import com.amazonaws.auth.{InstanceProfileCredentialsProvider,
	AWSCredentialsProviderChain}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions.EU_WEST_1
import com.amazonaws.services.stepfunctions.{AWSStepFunctionsClientBuilder,
	AWSStepFunctions}
import com.amazonaws.services.stepfunctions.model.{ListActivitiesRequest,
	GetActivityTaskRequest,	SendTaskSuccessRequest, SendTaskFailureRequest,
	GetActivityTaskResult, ActivityListItem}

// Reading and writing JSON
import play.api.libs.json.{JsError, JsSuccess, Json}


// ----- App ----- //

object Worker extends App {

	// ----- Properties

	val AWS_ACCOUNT = "developerPlayground"
	val SLEEP_TIME = 1000
	val MAX_RAND_NUMBER = 10


	// ----- Case Classes

	case class TaskInput(no_numbers: Int)
	case class TaskResult(nums: Seq[Int])

	implicit val taskInputReads = Json.reads[TaskInput]
	implicit val taskResultWrites = Json.writes[TaskResult]


	// ----- Private Methods

	// Generates a list of random numbers.
	private def randomList(noNumbers: Int) = {

		val r = scala.util.Random
		for (i <- 1 to noNumbers) yield r.nextInt(MAX_RAND_NUMBER)

	}

	// Builds an AWS client based on credentials present on the host.
	private def buildClient(): AWSStepFunctions = {

		val CredentialsProvider = new AWSCredentialsProviderChain(
			new ProfileCredentialsProvider(AWS_ACCOUNT),
		    new InstanceProfileCredentialsProvider(false)
		)

		AWSStepFunctionsClientBuilder.standard
			.withCredentials(CredentialsProvider)
			.withRegion(EU_WEST_1).build

	}

	// Performs the given task and returns the result.
	private def doTask(input: TaskInput): TaskResult = {

		val randomNumbers = randomList(input.no_numbers)
		TaskResult(nums=randomNumbers)

	}

	// Builds a task success request (because Java...).
	private def taskSuccess(task: GetActivityTaskResult, result: TaskResult) = {

		val resultJson = Json.toJson(result).toString

		new SendTaskSuccessRequest()
			.withOutput(resultJson)
			.withTaskToken(task.getTaskToken)

	}

	// Builds a task failure request (also because Java...).
	private def taskFailure(task: GetActivityTaskResult) = {
		new SendTaskFailureRequest().withTaskToken(task.getTaskToken)
	}

	// Attempts to extract the details of the task from JSON input.
	private def parseInput(task: GetActivityTaskResult): Option[TaskInput] = {

		val taskInput = Json.fromJson[TaskInput](Json.parse(task.getInput))

		taskInput match {
			case JsSuccess(input, _) => Some(input)
			case e: JsError => None
		}

	}

	// Attempts to perform the task, and returns result to the state machine.
	private def handleTask(
		client: AWSStepFunctions,
		task: GetActivityTaskResult) = {

		parseInput(task) match {

			case Some(input) => {
				val result = doTask(input)
				client.sendTaskSuccess(taskSuccess(task, result))
			}

			case None => client.sendTaskFailure(taskFailure(task))

		}

	}

	// Retrieves a task for a given activity.
	private def getTask(client: AWSStepFunctions, act: ActivityListItem) = {

		val arn = act.getActivityArn
		val taskRequest = new GetActivityTaskRequest().withActivityArn(arn)

		client.getActivityTask(taskRequest)

	}


	// ----- Run

	val client = buildClient
	val activities = client.listActivities(new ListActivitiesRequest)

	// Loops indefinitely, asking for available tasks from the state machine.
	while (true) {

		for (activity <- activities.getActivities) {

			val task = getTask(client, activity)

			if (task != null) handleTask(client, task)
			else Thread.sleep(SLEEP_TIME)

		}

	}

}
