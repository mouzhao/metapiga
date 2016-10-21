		
		
		
		
		
		
	
	
	__global__ void Hasegawa(int numCategories, int numStates, int numSites, double kappa,
                    double apRate, double* rates, double rateScaling, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
					double* PIj, double* equiFreq,  
                    double* branchLength,
                    int* rescalingNeeded, double* ufScaling, int* underflowEncountered,
					double* debugging, double* debugging2){
	
	//among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x;
    // index of the ancestral state at the current node
    int ancStateIdx = threadIdx.y;
	// numerical representations of states A, C, G and T.
	int A = 0;
	int C = 1;
	int G = 2;
	int T = 3;
	
	int ancSequenceIdx = categoryIdx * numStates * numSites + ancStateIdx * numSites + siteIdx;
	
	__shared__ double expA[8];
	__shared__ double expB[8];
	__shared__ double diag[8];
	__shared__ double ti[8];
	__shared__ double tv[8];
	__shared__ double scalingFactorsRecord[1024]; 
	
	if(siteBlockIdx == 0){
		for(int seq = 0; seq < 2; seq++){
			expA[seq * numStates + ancStateIdx] = exp(-(branchLength[seq]) * rateScaling * rates[categoryIdx] * apRate);
			expB[seq * numStates + ancStateIdx] = exp(-(branchLength[seq]) * rateScaling * rates[categoryIdx] * apRate * (1.0 + PIj[ancStateIdx] * (kappa - 1.0)));
		}
	}
	
	__syncthreads();
	
	if(siteBlockIdx == 0){
		for(int seq = 0; seq < 2; seq++){
			diag[seq * numStates + ancStateIdx] = equiFreq[ancStateIdx] + equiFreq[ancStateIdx]
							* ((1.0/PIj[ancStateIdx])-1.0) * expA[seq * numStates + ancStateIdx] +((PIj[ancStateIdx]-equiFreq[ancStateIdx])/PIj[ancStateIdx])
							* expB[seq * numStates + ancStateIdx];
			ti[seq * numStates + ancStateIdx] = equiFreq[ancStateIdx] + equiFreq[ancStateIdx]
							* ((1.0/PIj[ancStateIdx])-1.0) * expA[seq * numStates + ancStateIdx] - equiFreq[ancStateIdx]/PIj[ancStateIdx]
							* expB[seq * numStates + ancStateIdx];
			tv[seq * numStates + ancStateIdx] =  equiFreq[ancStateIdx] * (1.0 - expA[seq * numStates + ancStateIdx]);
		}
	}
	
	__syncthreads();
	
	double sumLeft = 0;
    double sumRight = 0;
	
	
	if(siteIdx < numSites){
		
		if(ancStateIdx == A){
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * diag[A];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * diag[numStates + A];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tv[C];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tv[numStates + C];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * ti[G];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * ti[numStates + G];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tv[T];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tv[numStates + T];
		}else if(ancStateIdx == C){
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tv[A];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tv[numStates + A];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * diag[C];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * diag[numStates + C];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tv[G];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tv[numStates + G];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * ti[T];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * ti[numStates + T];
		}else if(ancStateIdx == G){
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * ti[A];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * ti[numStates + A];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tv[C];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tv[numStates + C];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * diag[G];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * diag[numStates + G];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tv[T];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tv[numStates + T];
		}else if(ancStateIdx == T){
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tv[A];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tv[numStates + A];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * ti[C];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * ti[numStates + C];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tv[G];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tv[numStates + G];
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * diag[T];
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * diag[numStates + T];
		}
    
		double prod = sumLeft * sumRight;
	
		if(rescalingNeeded[0] != 0){
			// ... = (float)(prod/ufScaling[categoryIdx * numSites + siteIdx]); <- this doesn't work for some reason.
			//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
			seqAncNode[ancSequenceIdx] = (float)(prod*(1/ufScaling[categoryIdx * numSites + siteIdx]));
		}else{
			seqAncNode[categoryIdx * numStates * numSites +
					ancStateIdx * numSites + siteIdx] = (float)prod;
					scalingFactorsRecord[siteBlockIdx * numStates + ancStateIdx] = prod;
			if(prod < SMALLEST_FLOAT) underflowEncountered[0] = 1;
		}
	}
	
	__syncthreads();
	
	if(rescalingNeeded[0] == 0 && ancStateIdx == 0 && siteIdx < numSites){
		ufScaling[categoryIdx * numSites + siteIdx] = 0;
		for(int i = 0; i < numStates; i++){
		double stateValue = scalingFactorsRecord[siteBlockIdx * numStates + i];
			if(stateValue > ufScaling[categoryIdx * numSites + siteIdx]){
				ufScaling[categoryIdx * numSites + siteIdx] = stateValue;
			}
		}
	}
}
	
