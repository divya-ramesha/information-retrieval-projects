<?php
include 'SpellCorrector.php';
include 'simple_html_dom.php';
header('Content-Type: text/html; charset=utf-8');

$div=false;
$correct = "";
$correct1="";
$output = "";
$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
    $choice = isset($_REQUEST['sort'])? $_REQUEST['sort'] : "default";
    require_once('solr-php-client/Apache/Solr/Service.php');
    $solr = new Apache_Solr_Service('localhost', 8983, '/solr/mycore');
    if (get_magic_quotes_gpc() == 1)
    {
        $query = stripslashes($query);
    }
    try
    {
        if($choice == "default")
            $additionalParameters=array('sort' => '');
        else{
            $additionalParameters=array('sort' => 'pageRankFile desc');
        }
        $word = explode(" ", trim($query));
        $sze = sizeOf($word);
        for($i=0;$i<sizeOf($word);$i++){
            ini_set('memory_limit',-1);
            ini_set('max_execution_time', 300);
            $che = SpellCorrector::correct($word[$i]);
            if($correct!="") {
                $correct = $correct."+".trim($che);
            } else {
                $correct = trim($che);
            }
            $correct1 = $correct1." ".trim($che);
        }
        $correct1 = str_replace("+"," ",$correct);
        $div=false;
        if(strtolower($query)==strtolower($correct1)){
            $results = $solr->search($query, 0, $limit, $additionalParameters);
        }
        else {
            $div =true;
            $results = $solr->search($query, 0, $limit, $additionalParameters);
            $link = "http://localhost/index.php?q=$correct&sort=$choice";
            $output = "<span style='color:red;'>Did you mean: </span><b><i><a href='$link' style='text-decoration:none;'>$correct1</a></i></b>";
        }
    }
    catch (Exception $e)
    {
        die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
    }
}

?>
<html>
<head>
    <title>HW5 - PHP Solr Client</title>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
    <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
</head>
<body>
<h3 style="text-align:center;">PHP Solr Client - AutoSuggest + AutoComplete</h3>

<form  accept-charset="utf-8" method="get">
    <label for="q"><b>Search:</b></label>
    <input id="q" name="q" type="text" value="<?php $input = htmlspecialchars($query, ENT_QUOTES, 'utf-8');echo $input; ?>"/>
    <br/><br/>
    <input type="radio" name="sort" value="default" <?php if(isset($_REQUEST['sort']) && $choice == "default") { echo 'checked="checked"';} ?>>Default
    <input type="radio" name="sort" value="pagerank" <?php if(isset($_REQUEST['sort']) && $choice == "pagerank") { echo 'checked="checked"';} ?>>Page Rank
    <br/><br/>
    <input type="submit" value="Submit"/>
</form>
<script>
    $(function() {
        var URL_PREFIX = "http://localhost:8983/solr/mycore/suggest?q=";
        var URL_SUFFIX = "&wt=json&indent=true";
        $("#q").autocomplete({
            source : function(request, response) {
                var query = $("#q").val().toLowerCase().trim();
                var URL = URL_PREFIX + query + URL_SUFFIX;
                $.ajax({
                    url : URL,
                    success : function(data) {
                        var js =data.suggest.suggest;
                        var docs = JSON.stringify(js);
                        var jsonData = JSON.parse(docs);
                        var result =jsonData[query].suggestions;
                        var tags = [];
                        for(var z=0; z<result.length; z++) {
                            tags.push(result[z].term);
                        }
                        response(tags);
                    },
                    dataType : 'jsonp',
                    jsonp : 'json.wrf'
                });
            },
            minLength : 1
        })
    });
</script>
<?php
if($div){
    echo $output;
}
$count =0;
$prev="";
$arrayFromCSV =  array_map('str_getcsv', file('URLtoHTML_fox_news.csv'));
if ($results)
{
    $total = (int) $results->response->numFound;
    $start = min(1, $total);
    $end = min($limit, $total);
    if ($total > 0) {
        echo "  <div>Results $start -  $end of $total :</div>";
    }
    echo "  <ol>";
    foreach ($results->response->docs as $doc)
    {
        $id = $doc->id;
        $title = $doc->title;
        $desc = $doc->og_description;
        if($title=="" ||$title==null){
            $title = $doc->dc_title;
            if($title=="" ||$title==null)
                $title="N/A";

        }
        if($desc=="" ||$desc==null)
            $desc="N/A";
        $id2 = $id;
        $id = str_replace("/home/divya/solr-7.7.0/foxnews/","",$id);
        foreach($arrayFromCSV as $row1)
        {
            if($id==$row1[0])
            {
                $url = $row1[1];
                break;
            }
        }
        echo "  <li>
  <b>Title: </b><a href='$url' target='_blank'>$title</a></br>
  <b>URL: </b><a href='$url' target='_blank'>$url</a></br>
  <b>ID: </b> $id2<br/>
  <b>Description:</b> $desc<br/>
  </li><br/>";
    }
    echo "</ol>";
}
?>

</body>
</html>