use DBI;
#================================================================================
sub sql_connect
{ local ($host,$database,$username,$password)=@_;

        local $D = DBI->connect("DBI:mysql:$database:$host:3306", $username, $password)
		or die "Unable to connect: $DBI::errstr\n";

	return $D;
}
#================================================================================
sub execute_query
{ local ($D,$sql)=@_;

	debug("SQL : $sql");
	
	my $Q = $D->prepare($sql);
	$Q->execute();
	return $Q;
}
#================================================================================
sub sql_unescape
{
}
#================================================================================
sub sql_escape
{
}
#================================================================================
1;
