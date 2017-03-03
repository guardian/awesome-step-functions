package supplier

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
import com.amazonaws.services.stepfunctions.model.{StartExecutionRequest,
	ListStateMachinesRequest, StateMachineListItem}

// Reading and writing JSON
import play.api.libs.json.{JsError, JsSuccess, Json}


// ----- App ----- //

object Supplier extends App {

	// ----- Properties

	val AWS_ACCOUNT = "developerPlayground"
	val STATE_MACHINE_NAME = "SimplesWorkerOne"
	val NO_RANDOM_NUMBERS = 5


	// ----- Case Classes

	case class ExecutionInput(no_numbers: Int)

	implicit val executionInputWrites = Json.writes[ExecutionInput]


	// ----- Private Methods

	// Builds an AWS client based on credentials present on the host.
	private def buildClient() = {

		val CredentialsProvider = new AWSCredentialsProviderChain(
			new ProfileCredentialsProvider(AWS_ACCOUNT),
		    new InstanceProfileCredentialsProvider(false)
		)

		AWSStepFunctionsClientBuilder.standard
			.withCredentials(CredentialsProvider)
			.withRegion(EU_WEST_1).build

	}

	// Builds an execution request (because Java...).
	private def executionRequest(machine: StateMachineListItem) = {

		val input = ExecutionInput(NO_RANDOM_NUMBERS)
		val inputJson = Json.toJson(input).toString

		new StartExecutionRequest()
			.withInput(inputJson)
			.withStateMachineArn(machine.getStateMachineArn)

	}

	// Retrieves information about a state machine based on its name.
	private def retrieveStateMachine(client: AWSStepFunctions, name: String) = {

		val machines = client.listStateMachines(new ListStateMachinesRequest)
		machines.getStateMachines.find(_.getName == name)

	}


	// ----- Run

	val client = buildClient

	retrieveStateMachine(client, STATE_MACHINE_NAME) match {
		case Some(machine) => client.startExecution(executionRequest(machine))
		case None => println("No state machine found!")
	}

}
