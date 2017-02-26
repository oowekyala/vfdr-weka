#	Converts data in C4 5 format (a pair made of a .names and .data file) to ARFF format (a single .arff file).
#
#	LIMITS: Supports only basic continuous and nominal attributes, needs both .names and .data files to be in same directory and have the same name, the output file is created in the same directory, .
# 	USAGE: The user specifies the name of the dataset (either the path to one of the files, or the same path without file extension) either with the argument of the -i option or the first default parameter.
#	OPTIONS:
#		-i One of the input files
#		-d Only processes the names file (header).
#		-h Shows this tooltip


package datanames2arff;


use Getopt::Std;


getopts('dhi:');

if ($opt_h){
	usage();
	exit 0;
}

my $dataset_name = $opt_i || $ARGV[0];

# copy the data from the data file to the arff too
my $putdata = 0;

my $namesfile = "$dataset_name.names";
my $datafile = "$dataset_name.data";
my $outputfile = "$dataset_name.arff";


open( my $names_fh => $namesfile) or die "Could not open file '$(dataset_name).names' $!";
open(my $arff_fh, '>', $outputfile) or die "Could not open file '$(dataset_name).arff' for writing $!";

# HEADER

$dataset_name =~ s!^(.+/)++!!;
print $arff_fh "\@RELATION\t$dataset_name\n\n";

my @classvalues =();
while (my $line = <$names_fh>){
	if ($line =~ /^\s*\|.*/){ # comments
		$line =~ s/\|/%/;
		print $arff_fh "$line";
	} elsif ($line =~ /^\s*([ \w+\-]+,)+.+\./) { # class values
		chomp $line;
		while ($line =~ /([^.]++)[,.]/g) {
			push @classvalues, $1;
		}
	} elsif ($line =~ /^(.+):\s*continuous\./) {
		my $attname = $1;
		$attname =~ s/\s+/_/g;
		print $arff_fh "\@ATTRIBUTE\t$attname\tNUMERIC\n";
	} elsif ($line =~ /^(.+):/){
		my $attname = $1;
		$attname =~ s/\s+/_/g;
		
		my $attvalues = $line;
		chomp $attvalues;
		$attvalues =~ s/^(.+)://;
		$attvalues =~ s/\..*+//;
		print $arff_fh "\@ATTRIBUTE\t$attname\t{$attvalues }\n"
	}
}

print $arff_fh "\@ATTRIBUTE\tclass\t{";
while (scalar @classvalues > 0){
	my $val = pop @classvalues;
	print $arff_fh $val;
}
print $arff_fh "}\n";
close $names_fh;
print "Header written with no issue\n";

if (!$opt_d){
	open(my $data_fh, '<', $datafile) or die "Could not open file $datafile $!";

	print $arff_fh "\n\n\@DATA\n";

	while (my $line = <$data_fh>){
		print $arff_fh $line;
	}
	close $data_fh;
	close arff_fh;

	print "Data written with no issue\n";
	print "Success converting $namesfile and $datafile to $outputfile!\n";
}

sub usage {
	print "Converts data in C4 5 format (a pair made of a .names and .data file) to ARFF format (a single .arff file).\n";
	print "\tLIMITS:\tSupports only basic continuous and nominal attributes, needs both .names and .data files to be in same directory and have the same name, the output file is created in the same directory.\n";
 	print "\tUSAGE:\tThe user specifies the name of the dataset (either the path to one of the files, or the same path without file extension) either with the argument of the -i option or the first default parameter.\n";
	print "\tOPTIONS:\n";
	print "\t\t-i\tOne of the input files\n";
	print "\t\t-d\tOnly processes the names file (header).\n";
	print "\t\t-h\tShows this tooltip\n";
}
















