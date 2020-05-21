<?php
header('Content-Type: text/html; charset=utf-8');
$docs_limit = 10;
$query_term = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$page_rank_parameter = array(
	'sort' => 'pageRankFile desc'
);
$results = false;

if ($query_term)
{
 	require_once('solr-php-client/Apache/Solr/Service.php');
 	$solr = new Apache_Solr_Service('localhost', 8983, 'solr/mycore');
 	if (get_magic_quotes_gpc() == 1)
 	{
	 	$query_term = stripslashes($query_term);
 	}
 	try { 
		if($_REQUEST['sort'] == 'solr') {
			$results = $solr->search($query_term, 0, $docs_limit); 
		} else {
			$results = $solr->search($query_term, 0, $docs_limit, $page_rank_parameter);
		}
  	} catch (Exception $e) { 
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  	} 
}

$id_url_map = array();

$file = fopen("URLtoHTML_fox_news.csv","r");
if($file !== false){
	while($line = fgetcsv($file, 0, ","))
	{
		$id_url_map[$line['0']] = $line['1'];
	}
	fclose($file);
}

?>

<html> 
<head> 
	<title>Solr Client</title> 
</head>
<body> 
	<h2 align="center">PHP Solr Client</h2>
	<form accept-charset="utf-8" method="get"> 
		<br>
		<label for="q"><b>Search:</b></label> 
		<input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query_term, ENT_QUOTES, 'utf-8'); ?>"/> 
		<br><br>
		<table>
			<tr>
				<td>
					<input type="radio" name="sort" value="solr" <?php if(!isset($_REQUEST['sort']) || $_REQUEST['sort'] == 'solr' ) echo "checked"; ?>>
				</td>
				<td>Solr Lucene(Default)</td>
				<td style="width: 10px;"></td>
				<td>
					<input type="radio" name="sort" value="page_rank" <?php if($_REQUEST['sort'] == 'page_rank' ) echo "checked"; ?>>
				</td>
				<td>Page Rank</td>
			</tr>
		</table>
        <br>
		<input type="submit"/>
	</form> 

<?php 
if ($results) { 
	$total = (int) $results->response->numFound; 
	$start = min(1, $total); 
	$end = min($docs_limit, $total); 
?> 
<div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div> 
<ol> 
	<?php 
		foreach ($results->response->docs as $doc) {
	?> 
	<li> 
		 <table>
		<?php
		 
		$id = "N/A";
 		$url="N/A";
		$description = "N/A";
		$title = "N/A";

		foreach ($doc as $field => $value) { 
			if ($field == "id" || $field == "title" || $field == "og_description" || $field == "og_url") {
				$value = trim($value);
				if ($field == "id" ) {
					if ($value != "") {
						$id = $value;
					}
				}
				if ($field == "title") {
					if ($value != "") {
						$title = $value;
					}
				}
				if ($field == "og_description") {
					if ($value != "") {
						$description = $value;
					}
				}
				if ($field == "og_url") {
					if ($value != "") {
						$url = $value;
					}
				}
			}
		}
		if ($url == "N/A") {
			$url = $id_url_map[str_replace("/home/divya/solr-7.7.0/foxnews/", "", $id)];
		}

			echo "<tr>";        
			echo "Title: <a href='$url' target='_blank'>$title</a><br>";
			echo "URL: <a href='$url' target='_blank'>$url</a><br>";
			echo "ID: $id <br>";			
			echo "Description: $description <br><br>";
			echo "</tr>";
		 ?>

		</table> 
	</li> 
	<?php 
	} 
	?> 
</ol> 
<?php 
} 
?> 
</body> 
</html>
