import weka.classifiers.functions.SMO;
import weka.classifiers.bayes.*
import weka.classifiers.*
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.meta.Vote;
import weka.classifiers.rules.ZeroR;
import weka.filters.unsupervised.attribute.StringToWordVector
import weka.core.converters.ArffLoader;
import weka.core.stemmers.*
import weka.core.*


def textPreproc = { boolean countWords, boolean tf_idf ->
	
	StringToWordVector filter = new StringToWordVector()
	filter.setOutputWordCounts(countWords)
	filter.setWordsToKeep(10000);
	filter.setTFTransform(tf_idf);
	filter.setIDFTransform(tf_idf);
	filter.setNormalizeDocLength(new SelectedTag(0,StringToWordVector.TAGS_FILTER))
	filter.setLowerCaseTokens(false)
	filter.setUseStoplist(true)
	filter.setStopwords(null) // use default stopword list
	filter.setStemmer(new NullStemmer())
	filter.setMinTermFreq(1)
	filter.setAttributeNamePrefix("_")
	filter.setAttributeIndices("first")
	return filter;
	
}

def runExperiment = { String fileName, Classifier classifier, StringToWordVector filter ->

	ArffLoader arffLoader = new ArffLoader()
	arffLoader.setFile( new File(fileName) )
	Instances trainData = arffLoader.getDataSet()

	filter.setInputFormat(trainData)

	Instances vectorizedData = StringToWordVector.useFilter( trainData, filter );
	vectorizedData.setClassIndex(0)
	
	Evaluation eval = new Evaluation(vectorizedData)
	int folds = 10;
	eval.crossValidateModel( classifier, vectorizedData, 10, new Random(1));
	println( eval.toClassDetailsString() )
	println( eval.toSummaryString() )
}


def filterT1 = textPreproc(false,false);
def filterT2 = textPreproc(true,false);
def filterT3 = textPreproc(true,true);

def twt1 = "data/twt_1.arff"
def twt2 = "data/twt_2.arff"
def twt3 = "data/twt_3.arff"

def NB = new NaiveBayes()
def SMO = new SMO()
def ZR = new ZeroR()
Vote VOTE = new Vote();
VOTE.setClassifiers( [new NaiveBayes(), new SMO()] as Classifier[] )


[NB,SMO,ZR,VOTE].each{ classifier ->
	def t = 1
	[filterT1,filterT2,filterT3].each { filter ->
		[twt1,twt2,twt3].each { twt ->
			println "******************************************************"
			println "Classifier=$classifier\nT$t\n$twt"
			println "******************************************************"
			runExperiment(twt,classifier,filter)
		}
		t++
	}
}



