Groovy Monkey Plugin for Eclipse
================================
cloned by fmjrey
----------------

This project is a clone of the original project at [http://sourceforge.net/projects/groovy-monkey/](http://sourceforge.net/projects/groovy-monkey/), home at [http://groovy.codehaus.org/Groovy+Monkey](http://groovy.codehaus.org/Groovy+Monkey).
Its purpose is not to fork development of the original project but simply to make it easier to share some changes that hopefully will be merged back into the original.

*REBASE WARNING*
The only branches that will not be rebased are the main branches listed below. If you are cloning this repository you are strongly advised to create your own branches based on these. Other branches like topic branches may be rebased over time in order to keep track with upstream changes.

Main Branches
-------------

* [trunk](/fmjrey/Groovy-Monkey/tree/trunk) -- branch that tracks the original svn repository trunk
* [master](/fmjrey/Groovy-Monkey/tree/master) -- master branch, not used and identical to trunk at present
* [fmjrey](/fmjrey/Groovy-Monkey/tree/fmjrey) -- own master branch, which merges the other branches below and may contain other commits

Topic Branches
--------------

* [issue-3221121-editor-removes-certain-lines](/fmjrey/Groovy-Monkey/tree/issue-3221121-editor-removes-certain-lines) -- Fix for [issue 3221121](http://sourceforge.net/support/tracker.php?aid=3221121): editor removes certain lines when updating metadata.
* [version-qualifier](/fmjrey/Groovy-Monkey/tree/version-qualifier) -- added version qualifier on each plugin and feature version (e.g. 0.6.1.qualifier)
* [workspace-limitation-removed](/fmjrey/Groovy-Monkey/tree/workspace-limitation-removed) -- Include: metadata can now point to a project outside the workspace directory

Structure
---------
The structure is dentical to the [original svn repository on sourceforge](http://sourceforge.net/projects/groovy-monkey/develop).
Some 
The import was done by running svnimport.sh.
A few changes have been made in separate branches in each subproject.

Contributing
------------
If you want to provide a contribution it's best to do so by forking this project and make your changes inside your own new branch in your fork.
Having each change/feature in its own unique branch makes it easier for the original project to pull each branch independently, thus allowing cherry-picking branches before the need for cherry-picking commits.
Such branches are usually called "topic branches", as described [here](https://github.com/dchelimsky/rspec/wiki/Topic-Branches) and [here](http://stackoverflow.com/questions/284514/what-is-a-git-topic-branch).
You may also want to create your own "master" branch in which you can merge all your branches and other commits from elsewhere you may need for yourself.
To signal your changes you can create issues or pull requests in the original project and link to the corresponding branch in your fork.
When the original projects commits new changes, you need to bring these changes into your fork by rebasing your branches.
Once the topic branch has been merged into the original project, you probably want to delete it.
