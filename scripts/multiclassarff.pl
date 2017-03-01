
use strict;

my $filename = '../datafiles/testmultiabcd.arff';
open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";


print $fh "\@RELATION testabcd\n\n";

print $fh "\@ATTRIBUTE\tA\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tB\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tC\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tD\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tclass\t{true, false, undef}\n\n";

print $fh "\@DATA\n";
my @line;
for (my $i = 0; $i < 5000; $i++){
    for (my $j = 0; $j < 4; $j++){
        my $rand =  (int(rand(2)));
        push @line, $rand;
        print $fh "$rand,";
    }

    if (($line[0] == 1 || $line[1] == 1) && ($line[2] == 1 || $line[3] == 1)) {
        print $fh "true\n";
    } elsif (($line[0] == 1 && $line[1] == 1) || ($line[2] == 1 && $line[3] == 1)) {
    	print $fh "undef\n";
    }else{
        print $fh "false\n";
    }
    @line = ();
}



