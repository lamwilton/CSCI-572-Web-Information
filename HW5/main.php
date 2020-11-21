<?php
ini_set('display_startup_errors', true);
error_reporting(E_ERROR);
ini_set('display_errors', true);
ini_set('memory_limit', -1);

include 'SpellCorrector.php';

//it will output *october*
// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;
// Define additional parameters for pagerank
$additionalParameters = array( 'fl' => '*,field(pageRankFile)');
?>

<html>
  <head>
    <title>PHP Solr Client Example</title>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/smoothness/jquery-ui.css">
    <script src="//code.jquery.com/jquery-1.12.4.js"></script>
    <script src="//code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>

      <!-- Autocomplete script -->
      <script>
        
        var tags = [ "c++", "java", "php", "coldfusion", "javascript", "asp", "ruby" ];
        $( "#q" ).autocomplete({
          source: function( request, response ) {
                  var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( request.term ), "i" );

                  var queryTerm = $("#q").val();
                  var splits = queryTerm.split(" ");
                  var previousTerm = "";
                  var currTerm = queryTerm;
                  if(splits.length > 1) {
                    currTerm =  splits[splits.length - 1];
                    var lastIndex = queryTerm.lastIndexOf(" ");
                    previousTerm = queryTerm.substring(0, lastIndex);
                  }
                  var url = "http:\/\/localhost:8983/solr/myexample/suggest?q=" + currTerm + "&wt=json";
                  console.log(url);
                  console.log(currTerm);

                  $.ajax( {
                    url: url,
                    crossDomain: true,
                    dataType: "jsonp",
                    jsonp : 'json.wrf',
                    success: function( data ) {
                      var suggestionArr = JSON.parse(JSON.stringify(data)).suggest.suggest[currTerm].suggestions;
                      results = []
                      for (var i =0; i < suggestionArr.length; i++) {
                        if(previousTerm != "") {
                          results[i] = previousTerm + " "  + suggestionArr[i].term;
                        } else {
                            results[i] = suggestionArr[i].term;
                        }                      
                      }
                      
                      console.log(results);
                    
                    response( results );
                    }
                  } );

                  response( $.grep( tags, function( item ){
                      return matcher.test( item );
                  }) );
              }
        });

      </script>

      <input type="checkbox", name="box", value="Y">
      <label> Pagerank </label>
      <input type="submit"/>
      
    </form>
<?php    
if (isset($_GET['box'])) {
	$additionalParameters = array( 'sort' => 'pageRankFile desc', 'fl' => '*,field(pageRankFile)');
}

if ($query)
{
  $querycorr = SpellCorrector::correct($query);
  if ($querycorr != $query) {
  echo "Did you mean " ; ?>
  <a href="/main.php?q=<?php  echo $querycorr; ?>"><?php  echo $querycorr; ?> </a> <?php
  }
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');

  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
    $results = $solr->search($query, 0, $limit, $additionalParameters);
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>

<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php
  // iterate result documents
  foreach ($results->response->docs as $doc)
  {
?>
      <li>
        <table style="border: 1px solid black; text-align: left">
<?php
    // iterate document fields / values
    foreach ($doc as $field => $value) {
    	// Filter only fields of interest
    	if ($field == "id" || $field == "og_description") {
?>
          <tr>
            <th><?php echo htmlspecialchars($field, ENT_NOQUOTES, 'utf-8'); ?></th>
            <td><?php echo htmlspecialchars($value, ENT_NOQUOTES, 'utf-8'); ?></td>
          </tr>
<?php
    	}
    	// Clickable hyperlinks
    	if ($field == "og_url"|| $field == "title") {
    		?>
    		<tr>
            <th><?php echo htmlspecialchars($field, ENT_NOQUOTES, 'utf-8'); ?></th>
            <td><a href=<?php echo $doc->og_url;?>><?php echo htmlspecialchars($value, ENT_NOQUOTES, 'utf-8'); ?></a></td>
          	</tr>
          	<?php

    	}
	}
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