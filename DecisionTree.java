import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;

	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}


		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object 
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			if (datalist.size() >= minSizeDatalist){
				DTNode newNode = new DTNode();

				boolean sameLabel = true;
				int initial = datalist.get(0).y;
				for (Datum data: datalist){
					if (data.y != initial){
						sameLabel = false;
						break;
					}
				}
				if (sameLabel){
					newNode.label = initial;
					newNode.leaf = true;
					return newNode;
				}
				else{
					double[] answer = findBestSplit(datalist);
					if (answer[1] == -1){
						newNode.leaf = true;
						newNode.label = findMajority(datalist);
						return newNode;
					}
					newNode.attribute = (int) answer[0];
					newNode.threshold = answer[1];
					newNode.leaf = false;

					ArrayList<Datum> data1 = new ArrayList<>();
					ArrayList<Datum> data2 = new ArrayList<>();

					for (Datum data: datalist){
						if (data.x[newNode.attribute] < newNode.threshold){
							data1.add(data);
						}
						else{
							data2.add(data);
						}
					}

					newNode.left = fillDTNode(data1);
					newNode.right = fillDTNode(data2);

					return newNode;
				}
			}
			else{
				DTNode newNode = new DTNode();
				newNode.leaf = true;
				newNode.label = findMajority(datalist);
				//newNode.threshold = -1;
				return newNode;
			}



			//ADD CODE HERE
			//dummy code.  Update while completing the assignment.

		}
		private double calcAvgEntropy(ArrayList<Datum> datalist, int attribute, double threshold){
			ArrayList<Datum> dataLeft = new ArrayList<>();
			ArrayList<Datum> dataRight = new ArrayList<>();

			for (Datum data: datalist){
				if (data.x[attribute] < threshold){
					dataLeft.add(data);
				}
				else{
					dataRight.add(data);
				}
			}
			double leftEntropy = calcEntropy(dataLeft);
			double rightEntropy = calcEntropy(dataRight);
			double w1 = (double) (dataLeft.size())/datalist.size();
			double w2 = (double) (dataRight.size())/datalist.size();
            return w1*leftEntropy + w2*rightEntropy;
		}
		private double[] findBestSplit(ArrayList<Datum> datalist){
			double best_avg_entropy = Double.MAX_VALUE;
			int best_attribute = -1;
			double best_threshold = -1;
			double currentEntropy = calcEntropy(datalist);

			for (int i = 0; i < datalist.get(0).x.length; i++) {

				for (Datum data: datalist){
					double threshold = data.x[i];
					double currentAvgEntropy = calcAvgEntropy(datalist, i, threshold);

					if (best_avg_entropy>currentAvgEntropy){
						best_avg_entropy = currentAvgEntropy;
						best_attribute = i;
						best_threshold = threshold;
					}
				}


			}
			if (best_avg_entropy == currentEntropy){
				best_threshold = -1;
			}
			double[] answer = new double[2];
			answer[0] = best_attribute;
			answer[1] = best_threshold;

			return answer;
		}



		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {

			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}

			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			if (this.leaf) {
				return label;
			}
			if (attribute < 0 || xQuery.length <= attribute){
				return -1;
			}

			if (xQuery[attribute] < this.threshold) {
				return left.classifyAtNode(xQuery);
			} else {
				return right.classifyAtNode(xQuery);
			}
			//ADD CODE HERE
			//dummy code.  Update while completing the assignment.
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			if (this == dt2){
				return true;
			}
			if (dt2 == null || getClass() != dt2.getClass()){
				return false;
			}
			DTNode dt = (DTNode) dt2;

			if (this.leaf && dt.leaf){
				if (this.label == dt.label){
					return true;
				}
			}
			if (this.leaf != dt.leaf) return false;
			if (this.attribute != dt.attribute || Math.pow((this.threshold - dt.threshold),2) > 0.0000000001) return false;

			return (this.left.equals(dt.left) && this.right.equals(dt.right) );

			//ADD CODE HERE

			//dummy code.  Update while completing the assignment.
		}
	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
