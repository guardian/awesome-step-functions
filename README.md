# Awesome Step Functions

Demo code for the membership team's spike into step functions.

**Note:** The word 'awesome' in the name refers to the Awesome Product team, not the awesomeness of the code, which, as Roberto pointed out, probably wouldn't justify that title 🐰.

## Explanation of the Code

### Worker

The app in `Worker.scala` is the important bit in this demo code. It demonstrates how we can use the AWS Java SDK to implement a worker capable of communicating with a Step Functions State Machine, in order to process jobs.

This worker enters and indefinite loop and regularly polls the state machine for work. When there is a task to complete, the state machine sends the worker some JSON as input. When a task is complete, the worker replies to the state machine either with a success message that includes some JSON output, or with a simple failure message.

In the State Machine, activities are blueprints that describe possible tasks that a worker can subscribe to. In this app there is only one activity, but in a more realistic situation there are likely to be many. In this case we may want to create individual polling loops for each activity, and spin these up in different threads.

### Supplier

The app in `Supplier.scala` is very basic, it's used to send JSON inputs up to state machines in order to kick off executions. These executions are what then provide jobs for the worker.
