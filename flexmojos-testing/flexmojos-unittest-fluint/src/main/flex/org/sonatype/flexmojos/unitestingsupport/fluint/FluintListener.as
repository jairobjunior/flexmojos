/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.unitestingsupport.fluint
{
 	import flash.events.Event;

	import net.digitalprimates.fluint.monitor.TestCaseResult;
	import net.digitalprimates.fluint.monitor.TestMethodResult;
	import net.digitalprimates.fluint.monitor.TestMonitor;
	import net.digitalprimates.fluint.monitor.TestSuiteResult;
	import net.digitalprimates.fluint.tests.TestCase;
	import net.digitalprimates.fluint.tests.TestSuite;
	import net.digitalprimates.fluint.ui.TestEnvironment;
	import net.digitalprimates.fluint.ui.TestRunner;


	import org.sonatype.flexmojos.test.report.ErrorReport;
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;
	import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

	public class FluintListener implements UnitTestRunner
	{

		private var _testMonitor:TestMonitor=new TestMonitor();
		private var _testSuite:TestSuite=new TestSuite();

		private var _socketReporter:SocketReporter;
		
		public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}

		public function FluintListener(tests:Array=null, socketReporter:SocketReporter=null)
		{
			for each (var test:Class in tests)
			{
				var testCase:*=new test();
				if (testCase is TestCase)
				{
					_testSuite.addTestCase(testCase);
				}
			}
			
			this._socketReporter = socketReporter;
		}

		public function run(tests:Array):int
		{
			var listener:FluintListener=new FluintListener(tests,_socketReporter);
			return listener.runTests();
		}

		public function runTests():int
		{
			var testRunner:TestRunner=new TestRunner(_testMonitor);
			testRunner.testEnvironment=new TestEnvironment();
			testRunner.startTests(_testSuite);
			testRunner.addEventListener(TestRunner.TESTS_COMPLETE, handleTestsComplete);
			return testRunner.getTestCount();
		}

		private function handleTestsComplete(event:Event):void
		{
			var suiteResult:TestSuiteResult=_testMonitor.getTestSuiteResult(_testSuite);
			var testResults:Array=suiteResult.children.toArray();
			testResults.forEach(reportTestCaseResult);
		}

		private function reportTestCaseResult(element:*, index:int, arr:Array):void
		{
			var testCaseResult:TestCaseResult=TestCaseResult(element);
			var testMethodResults:Array=testCaseResult.children.toArray();
			for each (var testMethodResult:TestMethodResult in testMethodResults)
			{
				reportTestMethodResult(testCaseResult, testMethodResult);
			}
		}

		private function reportTestMethodResult(testCaseResult:TestCaseResult, testMethodResult:TestMethodResult):void
		{
			startTest(testCaseResult, testMethodResult);

			if (testMethodResult.errored)
			{
				addError(testCaseResult, testMethodResult);
			}

			if (testMethodResult.failed)
			{
				addFailure(testCaseResult, testMethodResult);
			}
			endTest(testCaseResult, testMethodResult);
		}


		private function startTest(testCaseResult:TestCaseResult, testMethodResult:TestMethodResult):void
		{
			_socketReporter.addMethod(testCaseResult.qualifiedClassName, testMethodResult.displayName);
		}

		private function endTest(testCaseResult:TestCaseResult, testMethodResult:TestMethodResult):void
		{
			_socketReporter.testFinished(testCaseResult.qualifiedClassName, testMethodResult.testDuration);
		}

		private function addError(testCaseResult:TestCaseResult, testMethodResult:TestMethodResult):void
		{
			var error:Error=testMethodResult.error;
			var failure:ErrorReport=new ErrorReport();
			failure.type=ClassnameUtil.getClassName(error);
			failure.message=error.message;

			if (failure.message == "Setup/Teardown Error")
			{
				//in case setup or teardown fails, the testCaseResult has the actual stackTrace
				failure.stackTrace=testCaseResult.traceInformation;
			}
			else
				failure.stackTrace=error.getStackTrace();

			_socketReporter.addError(testCaseResult.qualifiedClassName, testMethodResult.displayName, failure);
		}

		private function addFailure(testCaseResult:TestCaseResult, testMethodResult:TestMethodResult):void
		{
			var error:Error=testMethodResult.error;
			var failure:ErrorReport=new ErrorReport();
			failure.type=ClassnameUtil.getClassName(error);
			failure.message=error.message;
			failure.stackTrace=error.getStackTrace();
			_socketReporter.addFailure(testCaseResult.qualifiedClassName, testMethodResult.displayName, failure);
		}

	}
}