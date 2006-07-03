#!/usr/bin/perl
#
# Run this script from Eclipse as an external tool with the working directory
# being the project's root directory. Use the "refresh resources in the project
# containing the selected resource" option. The ssh requires public key credentials
# on the remote host in order to run without a password prompt.
#
use strict;

system( "rm -rf /tmp/update" );
system( "mkdir /tmp/update" );
system( "cp -R . /tmp/update" );

my @unwanted = grep !/((features)|(plugins)|(html)|(xml)|(jar))$/, </tmp/update/*>;
my $unwanted = join " ", @unwanted;
print "removing $unwanted\n";
system ( "rm -rf $unwanted" );

@unwanted = grep !/((xml)|(jar))$/, </tmp/update/*/*>;
$unwanted = join " ", @unwanted;
print "removing $unwanted\n";
system ( "rm -rf $unwanted" );

print "removing .project\n";
system ( "rm -rf /tmp/update/.project" );

my $username = "UNKNOWN_USER";
$_ = `whoami`;
$username = "wcunningh" if /ward/;
$username = "bfreeman" if /bjorn/;

my $cmd = "rsync -e ssh -av /tmp/update ${username}\@download1.eclipse.org:/home/data/httpd/download.eclipse.org/technology/dash/";
print "$cmd\n";
system( $cmd );

# system( "rm -rf /tmp/update" );
