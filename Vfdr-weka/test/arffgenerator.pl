
use strict;

my $filename = 'testabcd.arff';
open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";


print $fh "\@RELATION testabcd\n\n";

print $fh "\@ATTRIBUTE\ta\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tb\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tc\t{0, 1}\n";
print $fh "\@ATTRIBUTE\td\t{0, 1}\n";
print $fh "\@ATTRIBUTE\tclass\t{true, false}\n\n";

print $fh "\@DATA\n";
my @line;
for (my $i = 0; $i < 4000; $i++){
    for (my $j = 0; $j < 4; $j++){
        my $rand =  (int(rand(2)));
        push @line, $rand;
        print $fh "$rand,";
    }
 #    print $fh @line;

    if (($line[0] == 1 || $line[1] == 1) && ($line[2] == 1 || $line[3] == 1)) {
       
        print $fh "true\n";
    } else {
        print $fh "false\n";
    }
    @line=();
}



