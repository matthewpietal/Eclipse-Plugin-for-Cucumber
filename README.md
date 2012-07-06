# Eclipse-Plugin-for-Cucumber
===========================

Enables real-time validation and editing support for Cucumber feature files in Eclipse.

## Installation (for those that just want the binary version)
In Eclipse, use the "Help->Install New Software" menu item.  Click the "Add..." button.  Use the appropriate location below:
* Eclipse 3.7: https://raw.github.com/matthewpietal/Eclipse-Plugin-for-Cucumber/master/info.cukes.editor.ide.site_3.x/
* Eclipse 4.2: https://raw.github.com/matthewpietal/Eclipse-Plugin-for-Cucumber/master/info.cukes.editor.ide.site_4.x/

## Features
* Syntax Highlighting
* Validation: Scans your workspace for potential matching rules.  Will give you a warning if no rule is found.
* Rule suggestion: Type Given, When, Then, followed by CTRL+space to see all matching rules defined in your workspace. 
* Jump to defintion: For rules that match, hit F3 to jump to the Java code defined for that rule

## Project Layout
There are a few major sections to the code:
* _info.cukes.editor.ide_: This is the source code for the actual Eclipse plugin.
* _info.cukes.editor.ide.playground_: An Eclipse RCP test application.  This small app will open with the plugin installed as well as a sample feature file.  Will allow you to see how your changes to the Cucumber plugin are working in a live test environment.
* _info.cukes.editor.ide.feature_: Eclipse RCP feature project.  Needed for installation.
* _info.cukes.editor.ide.site_3.x_: Update site for 3.x Eclipse versions.  3.7 or higher required.
* _info.cukes.editor.ide.site_4.x_: Update site for 4.x Eclipse versions.
