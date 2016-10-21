#include "math.h"
#define SMALLEST_FLOAT 1.175494351E-38
#define MAX_ELEMENTS_PER_BLOCK 2048
#define NUM_BANKS 32
#define LOG_NUM_BANKS 5

#ifdef ZERO_BANK_CONFLICTS
#define CONFLICT_FREE_OFFSET(n)\
		((n) >> NUM_BANKS + (n) >> (2 * LOG_NUM_BANKS))
#else
#define CONFLICT_FREE_OFFSET(n)((n) >> LOG_NUM_BANKS)
#endif

extern "C"
__global__ void JukesCantorGpu(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double apRate, double* rates, double rateScaling, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
                    double blLeft, double blRight,
                    double* ufScaling){
    
    
    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x;
    // index of the ancestral state at the current node
    int ancStateIdx = threadIdx.y;



	__shared__ double ancSequenceSharedRecord[1024];
	__shared__ float leftPartialLikelihoods[1024];
	__shared__ float rightPartialLikelihoods[1024];
	__shared__ double ufScalingSharedMem[512];
	//__shared__ float parentPartialLikelihoods[1024];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		leftPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeLeft[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
		rightPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeRight[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}

    double categoryRate = rates[categoryIdx];
	
	double expLeft = exp(-blLeft * rateScaling * categoryRate * apRate);
    double diagLeft  = 0.25 + (0.75 * expLeft);
    double offdiagLeft = 0.25 - (0.25 * expLeft);

    double expRight = exp(-blRight * rateScaling * categoryRate * apRate);
    double diagRight  = 0.25 + (0.75 * expRight);
    double offdiagRight = 0.25 - (0.25 * expRight);

    int ancSequenceIdx = categoryIdx * numStates * numSitesWithPadding + ancStateIdx * numSitesWithPadding + siteIdx;

	double sumLeft = 0;
	double sumRight = 0;
	if(siteIdx < numSitesWithPadding){

		for(int descStateIdx = 0; descStateIdx < numStates; descStateIdx++){
			sumLeft += leftPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * 
										((descStateIdx == ancStateIdx) ? diagLeft : offdiagLeft);
                                
			sumRight += rightPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * 
										((descStateIdx == ancStateIdx) ? diagRight : offdiagRight);
		}

		double prod = sumLeft * sumRight;
		ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx] = prod;

	}
	
	__syncthreads();
	
	if(ancStateIdx == 0 && siteIdx < numSitesWithPadding){
		double maxValue = 0;		
		for(int i = 0; i < numStates; i++){
			double stateValue = ancSequenceSharedRecord[siteBlockIdx * numStates + i];
			if(stateValue > maxValue){
				maxValue = stateValue;
			}
		}
		if(siteIdx < numSites){
			ufScaling[categoryIdx * numSitesWithPadding + siteIdx] *= maxValue;
		}
		
		ufScalingSharedMem[siteBlockIdx] = maxValue;
	}
	__syncthreads();
		
	if(siteIdx < numSitesWithPadding){
		// ... = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]/ufScaling[categoryIdx * numSitesWithPadding + siteIdx]); <- this doesn't work for some reason.
		//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
		if(siteIdx < numSites){
			seqAncNode[ancSequenceIdx] = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]*(1/ufScalingSharedMem[siteBlockIdx]));
		}else{
			seqAncNode[ancSequenceIdx] = 0;
		}
	}
	

}

extern "C"
__global__ void PoissonGpu(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double apRate, double* rates, double rateScaling, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
                    double blLeft, double blRight,
                    double* ufScaling){
    
    
    
    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x;
    // index of the ancestral state at the current node
    int ancStateIdx = threadIdx.y;



	__shared__ double ancSequenceSharedRecord[1024];
	__shared__ float leftPartialLikelihoods[1024];
	__shared__ float rightPartialLikelihoods[1024];
	__shared__ double ufScalingSharedMem[512];
	//__shared__ float parentPartialLikelihoods[1024];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		leftPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeLeft[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
		rightPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeRight[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}

    double categoryRate = rates[categoryIdx];
	
	double expLeft = exp(-blLeft * rateScaling * categoryRate * apRate);
    double diagLeft  = 0.05 + (0.95 * expLeft);
    double offdiagLeft = 0.05 - (0.05 * expLeft);

    double expRight = exp(-blRight * rateScaling * categoryRate * apRate);
    double diagRight  = 0.05 + (0.95 * expRight);
    double offdiagRight = 0.05 - (0.05 * expRight);

    int ancSequenceIdx = categoryIdx * numStates * numSitesWithPadding + ancStateIdx * numSitesWithPadding + siteIdx;

	double sumLeft = 0;
	double sumRight = 0;
	if(siteIdx < numSitesWithPadding){

		for(int descStateIdx = 0; descStateIdx < numStates; descStateIdx++){
			sumLeft += leftPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * 
										((descStateIdx == ancStateIdx) ? diagLeft : offdiagLeft);
                                
			sumRight += rightPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * 
										((descStateIdx == ancStateIdx) ? diagRight : offdiagRight);
		}

		double prod = sumLeft * sumRight;
		ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx] = prod;

	}
	
	__syncthreads();
	
	if(ancStateIdx == 0 && siteIdx < numSitesWithPadding){
		double maxValue = 0;		
		for(int i = 0; i < numStates; i++){
			double stateValue = ancSequenceSharedRecord[siteBlockIdx * numStates + i];
			if(stateValue > maxValue){
				maxValue = stateValue;
			}
		}
		if(siteIdx < numSites) ufScaling[categoryIdx * numSitesWithPadding + siteIdx] *= maxValue;
		ufScalingSharedMem[siteBlockIdx] = maxValue;
	}
	__syncthreads();
		
	if(siteIdx < numSitesWithPadding){
		// ... = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]/ufScaling[categoryIdx * numSitesWithPadding + siteIdx]); <- this doesn't work for some reason.
		//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
		if(siteIdx < numSites){
			seqAncNode[ancSequenceIdx] = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]*(1/ufScalingSharedMem[siteBlockIdx]));
		}else{
			seqAncNode[ancSequenceIdx] = 0;
		}
	}
	

}


extern "C"
__global__ void KimuraGpu(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double kappa, double apRate, double* rates, double rateScaling, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
                    double blLeft, double blRight,
                    double* ufScaling){
    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x;
    // index of the ancestral state at the current node
    int ancStateIdx = threadIdx.y;

	// State indexes
	int A = 0;
	int C = 1;
	int G = 2;
	int T = 3;


	//Shared memory initialization
	__shared__ double ancSequenceSharedRecord[1024];
	__shared__ float leftPartialLikelihoods[1024];
	__shared__ float rightPartialLikelihoods[1024];
	__shared__ double ufScalingSharedMem[512];
	//__shared__ float parentPartialLikelihoods[1024];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		leftPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeLeft[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
		rightPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeRight[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}

    double categoryRate = rates[categoryIdx];
	
	float expLeftA = exp(-(blLeft) * rateScaling * categoryRate * apRate);
	float expLeftB = exp(-(blLeft) * rateScaling * categoryRate * apRate * ((kappa + 1) / 2));
	float diagLeft  = 0.25 + (0.25 * expLeftA) + (0.5 * expLeftB);
	float tiLeft = 0.25 + (0.25 * expLeftA) - (0.5 * expLeftB);
		
	float tvLeft = 0.25 - (0.25 * expLeftA);
		
		
	float expRightA = exp(-(blRight) * rateScaling * categoryRate * apRate);
	float expRightB = exp(-(blRight) * rateScaling * categoryRate * apRate * ((kappa + 1) / 2));
	float diagRight  = 0.25 + (0.25 * expRightA) + (0.5 * expRightB);
	float tiRight = 0.25 + (0.25 * expRightA) - (0.5 * expRightB);
		
	float tvRight = 0.25 - (0.25 * expRightA);

    int ancSequenceIdx = categoryIdx * numStates * numSitesWithPadding + ancStateIdx * numSitesWithPadding + siteIdx;

	double sumLeft = 0;
	double sumRight = 0;
	if(siteIdx < numSitesWithPadding){

		if(ancStateIdx == A){
			/*sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * diagLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * diagRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tiLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tiRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tvRight;
			//============================================================================================*/
			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * diagLeft;
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * diagRight;
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tiLeft;
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tiRight;
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tvRight;
			
		}else if(ancStateIdx == C){
			/*sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * diagLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * diagRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tiLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tiRight;
			//==============================================================================================*/
			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * diagLeft;
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * diagRight;
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tiLeft;
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tiRight;
		}else if(ancStateIdx == G){
			/*sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tiLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tiRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * diagLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * diagRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * tvRight;
			//===============================================================================================*/
			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tiLeft;
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tiRight;
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * diagLeft;
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * diagRight;
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tvRight;
		}else if(ancStateIdx == T){
			/*sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + A * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tiLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + C * numSites + siteIdx] * tiRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tvLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + G * numSites + siteIdx] * tvRight;
			sumLeft += seqNodeLeft[categoryIdx * numStates * numSites + T * numSites + siteIdx] * diagLeft;
			sumRight += seqNodeRight[categoryIdx * numStates * numSites + T * numSites + siteIdx] * diagRight;
			//===============================================================================================*/
			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tiLeft;
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tiRight;
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tvLeft;
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tvRight;
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * diagLeft;
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * diagRight;
		}

		double prod = sumLeft * sumRight;
		ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx] = prod;

	}
	
	__syncthreads();
	
	if(ancStateIdx == 0 && siteIdx < numSitesWithPadding){
		double maxValue = 0;		
		for(int i = 0; i < numStates; i++){
			double stateValue = ancSequenceSharedRecord[siteBlockIdx * numStates + i];
			if(stateValue > maxValue){
				maxValue = stateValue;
			}
		}
		if(siteIdx < numSites) ufScaling[categoryIdx * numSitesWithPadding + siteIdx] *= maxValue;
		ufScalingSharedMem[siteBlockIdx] = maxValue;
	}
	__syncthreads();
		
	if(siteIdx < numSitesWithPadding){
		// ... = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]/ufScaling[categoryIdx * numSitesWithPadding + siteIdx]); <- this doesn't work for some reason.
		//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
		if(siteIdx < numSites){
			seqAncNode[ancSequenceIdx] = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]*(1/ufScalingSharedMem[siteBlockIdx]));
		}else{
			seqAncNode[ancSequenceIdx] = 0;
		}
	}
}

extern "C"
__global__ void HasegawaGPU(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double kappa, double apRate, double* rates, double rateScaling, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
					double* PIj, double* equiFreq,
                    double* branchLengths,
                    double* ufScaling){
    
    
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
	

	//Shared memory initialization
	__shared__ double ancSequenceSharedRecord[1024];
	__shared__ float leftPartialLikelihoods[1024];
	__shared__ float rightPartialLikelihoods[1024];
	__shared__ double ufScalingSharedMem[512];
	
	__shared__ double expA[8];
	__shared__ double expB[8];
	__shared__ double diag[8];
	__shared__ double ti[8];
	__shared__ double tv[8];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		leftPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeLeft[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
		rightPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeRight[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}

    double categoryRate = rates[categoryIdx];
	
	if(siteBlockIdx == 0){
		for(int branch = 0; branch < 2; branch++){
			expA[branch * numStates + ancStateIdx] = exp(-(branchLengths[branch]) * rateScaling * categoryRate * apRate);
			expB[branch * numStates + ancStateIdx] = exp(-(branchLengths[branch]) * rateScaling * categoryRate * apRate * (1.0 + PIj[ancStateIdx] * (kappa - 1.0)));
			diag[branch * numStates + ancStateIdx] = equiFreq[ancStateIdx] + equiFreq[ancStateIdx]
							* ((1.0/PIj[ancStateIdx])-1.0) * expA[branch * numStates + ancStateIdx] +((PIj[ancStateIdx]-equiFreq[ancStateIdx])/PIj[ancStateIdx])
							* expB[branch * numStates + ancStateIdx];
			ti[branch * numStates + ancStateIdx] = equiFreq[ancStateIdx] + equiFreq[ancStateIdx]
							* ((1.0/PIj[ancStateIdx])-1.0) * expA[branch * numStates + ancStateIdx] - equiFreq[ancStateIdx]/PIj[ancStateIdx]
							* expB[branch * numStates + ancStateIdx];
			tv[branch * numStates + ancStateIdx] =  equiFreq[ancStateIdx] * (1.0 - expA[branch * numStates + ancStateIdx]);
		}
	}
	
	__syncthreads();

    int ancSequenceIdx = categoryIdx * numStates * numSitesWithPadding + ancStateIdx * numSitesWithPadding + siteIdx;

	double sumLeft = 0;
	double sumRight = 0;
	if(siteIdx < numSitesWithPadding){

		if(ancStateIdx == A){

			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * diag[A];
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * diag[numStates + A];
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tv[C];
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tv[numStates + C];
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * ti[G];
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * ti[numStates + G];
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tv[T];
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tv[numStates + T];
		}else if(ancStateIdx == C){

			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tv[A];
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tv[numStates + A];
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * diag[C];
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * diag[numStates + C];
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tv[G];
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tv[numStates + G];
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * ti[T];
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * ti[numStates + T];
		}else if(ancStateIdx == G){

			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * ti[A];
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * ti[numStates + A];
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tv[C];
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * tv[numStates + C];
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * diag[G];
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * diag[numStates + G];
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tv[T];
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * tv[numStates + T];
		}else if(ancStateIdx == T){

			sumLeft += leftPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tv[A];
			sumRight += rightPartialLikelihoods[A * blockDim.x + siteBlockIdx] * tv[numStates + A];
			sumLeft += leftPartialLikelihoods[C * blockDim.x + siteBlockIdx] * ti[C];
			sumRight += rightPartialLikelihoods[C * blockDim.x + siteBlockIdx] * ti[numStates + C];
			sumLeft += leftPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tv[G];
			sumRight += rightPartialLikelihoods[G * blockDim.x + siteBlockIdx] * tv[numStates + G];
			sumLeft += leftPartialLikelihoods[T * blockDim.x + siteBlockIdx] * diag[T];
			sumRight += rightPartialLikelihoods[T * blockDim.x + siteBlockIdx] * diag[numStates + T];
		}

		double prod = sumLeft * sumRight;
		ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx] = prod;
		//debugging[ancSequenceIdx] = prod;
		// debugging[A] = ti[A];
		// debugging[numStates + A] = ti[numStates + A];
		// debugging[C] = ti[C];
		// debugging[numStates + C] = ti[numStates + C];
		// debugging[G] = ti[G];
		// debugging[numStates + G] = ti[numStates + G];
		// debugging[T] = ti[T];
		// debugging[numStates + T] = ti[numStates + T];

	}
	
	__syncthreads();
	
	if(ancStateIdx == 0 && siteIdx < numSitesWithPadding){
		double maxValue = 0;		
		for(int i = 0; i < numStates; i++){
			double stateValue = ancSequenceSharedRecord[siteBlockIdx * numStates + i];
			if(stateValue > maxValue){
				maxValue = stateValue;
			}
		}
		if(siteIdx < numSites) ufScaling[categoryIdx * numSitesWithPadding + siteIdx] *= maxValue;
		ufScalingSharedMem[siteBlockIdx] = maxValue;
	}
	__syncthreads();
		
	if(siteIdx < numSitesWithPadding){
		// ... = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]/ufScaling[categoryIdx * numSitesWithPadding + siteIdx]); <- this doesn't work for some reason.
		//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
		if(siteIdx < numSites){
			seqAncNode[ancSequenceIdx] = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]*(1/ufScalingSharedMem[siteBlockIdx]));
		}else{
			seqAncNode[ancSequenceIdx] = 0;
		}
	}
	

}

extern "C"
__global__ void GTRGPU(int numCategories, int numStates, int numSitesWithPadding, int numSites, 
                    float* seqNodeLeft, float* seqNodeRight, float* seqAncNode,
					double* TPMleft, double* TPMright,
                    double* ufScaling, int sequence_split_offset){
    
    
    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x;
    // index of the ancestral state at the current node
    int ancStateIdx = threadIdx.y;



	__shared__ double ancSequenceSharedRecord[1024];
	__shared__ float leftPartialLikelihoods[1024];
	__shared__ float rightPartialLikelihoods[1024];
	__shared__ double ufScalingSharedMem[512];
	__shared__ double sMatrixColumnLeft[64];
	__shared__ double sMatrixColumnRight[64];

	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		leftPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeLeft[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
		rightPartialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = seqNodeRight[siteIdx + ancStateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}

    int ancSequenceIdx = categoryIdx * numStates * numSitesWithPadding + ancStateIdx * numSitesWithPadding + siteIdx;

	double sumLeft = 0;
	double sumRight = 0;
	

	for(int descStateIdx = 0; descStateIdx < numStates; descStateIdx++){
		
		if(threadIdx.x == 0){
			sMatrixColumnLeft[ancStateIdx] = TPMleft[categoryIdx * numStates * numStates + ancStateIdx * numStates + descStateIdx];
		}else if(threadIdx.x == 1){
			sMatrixColumnRight[ancStateIdx] = TPMright[categoryIdx * numStates * numStates + ancStateIdx * numStates + descStateIdx];
		}
		
		__syncthreads();
		
		if(siteIdx < numSites){
			sumLeft += leftPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * sMatrixColumnLeft[ancStateIdx];
                               
			sumRight += rightPartialLikelihoods[descStateIdx * blockDim.x + siteBlockIdx] * sMatrixColumnRight[ancStateIdx];
		}
		__syncthreads();
	}

	double prod = sumLeft * sumRight;
	ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx] = prod;
	
	__syncthreads();
	
	if(ancStateIdx == 0 && siteIdx < numSitesWithPadding){
	double maxValue = 0;		
	for(int i = 0; i < numStates; i++){
		double stateValue = ancSequenceSharedRecord[siteBlockIdx * numStates + i];
		if(stateValue > maxValue){
			maxValue = stateValue;
		}
	}
	int scaling_split_offset = sequence_split_offset * numCategories;
	
	if(siteIdx < numSites) ufScaling[categoryIdx * numSitesWithPadding + siteIdx + scaling_split_offset] *= maxValue;
	ufScalingSharedMem[siteBlockIdx] = maxValue;
	}
	__syncthreads();
		
	if(siteIdx < numSitesWithPadding){
		// ... = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]/ufScaling[categoryIdx * numSitesWithPadding + siteIdx]); <- this doesn't work for some reason.
		//Gives NaN as a result when really small numbers are used (1E-37 or smaller).
		if(siteIdx < numSites){
			seqAncNode[ancSequenceIdx] = (float)(ancSequenceSharedRecord[siteBlockIdx * numStates + ancStateIdx]*(1/ufScalingSharedMem[siteBlockIdx]));
		}else{
			seqAncNode[ancSequenceIdx] = 0;
		}
		
	}
	

}


extern "C"
__global__ void transMatrixCalc(int n, double* ad, double* bd, double* ed, double* TPMleft, double* TPMright,
									double blLeft, double blRight, double catRate, double apRate, int cat) {
									
	//MAKE IT FOR LEFT AND RIGHT
    __shared__ double as[32][32];
    __shared__ double bs[32][32];
	__shared__ double es[32];

    int tx = threadIdx.x;
    int ty = threadIdx.y;
    
    int x = (blockIdx.x * blockDim.x) + tx;
    int y = (blockIdx.y * blockDim.y) + ty;
        
    double vLeft = 0.0;
	double vRight = 0.0;
    
    int yn = y * n;
    int s = (n + 31) / 32;
	//int wholeBlocks = n/32;
    for(int m=0; m<s; m++) {
		
		int m32 = m * 32;

		as[ty][tx] = ad[yn + (m32 + tx)];
		bs[ty][tx] = bd[(m32 + ty) * n + x];
		es[tx] = ed[m32 + tx];

        __syncthreads();

		//skratiti broj iteracija
		for(int i=0; i+m32<n && i<32; i++) {
			double eigenExponent = exp(blLeft * catRate * apRate * es[i]);
			vLeft += as[ty][i] * eigenExponent * bs[i][tx];
			eigenExponent = exp(blRight * catRate * apRate * es[i]);
			vRight += as[ty][i] * eigenExponent * bs[i][tx];
		}

        __syncthreads();
    }
    if(x < n && y < n){
		TPMleft[cat*n*n + yn + x] = vLeft;
		TPMright[cat*n*n + yn + x] = vRight;
		//TPMleft[cat*n*n + yn + x] = cat;
		//TPMright[cat*n*n + yn + x] = catRate;
	}
}

extern "C"
__global__ void initUnderflowScaling(double* ufScaling, double value, int numElements){
	int elementIdx = blockIdx.x * blockDim.x + threadIdx.x;
	if(elementIdx < numElements){
		ufScaling[elementIdx] = (double)1.0;
	}
}

extern "C"
__global__ void reduceStates(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double pInv, double* equiFreq,
                    float* sequence, double* ufScaling, int ufScalingOffset){
    

    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x; 
    // index of the ancestral state at the current node
    int stateIdx = threadIdx.y;



	__shared__ float partialLikelihoods[1024];
	__shared__ double sharedDoubleBuffer[1024];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		partialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = sequence[siteIdx + stateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}
	
	
	if(siteIdx < numSites){	
		sharedDoubleBuffer[stateIdx * blockDim.x + siteBlockIdx] = partialLikelihoods[stateIdx * blockDim.x + siteBlockIdx] * equiFreq[stateIdx]
											* ((1.0 - pInv)/(double)(numCategories)) * ufScaling[categoryIdx * numSitesWithPadding + siteIdx + ufScalingOffset];
	}
	
	__syncthreads();
	
	// Reduce states here
	if(stateIdx == 0 && siteIdx < numSites){
		double cellLikelihoodAccumulator = 0;
		for(int st = 0; st < numStates; st++){
			cellLikelihoodAccumulator += sharedDoubleBuffer[st * blockDim.x + siteBlockIdx];
		}
		ufScaling[categoryIdx * numSitesWithPadding + siteIdx + ufScalingOffset] = cellLikelihoodAccumulator;
	}
}

extern "C"
__global__ void reduceCategories(int numCategories, int numSites, int numSitesWithPadding, double pInv, double* ufScaling, double* invSites, int* weights, int split_offset){
	int categoryIdx = threadIdx.y;
	int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	int siteBlockIdx = threadIdx.x;
	
	__shared__ double sharedDoubleBuffer[1024];
	
	if(siteIdx < numSites){
		sharedDoubleBuffer[categoryIdx * blockDim.x + siteBlockIdx] = ufScaling[categoryIdx * numSitesWithPadding + siteIdx + (split_offset*numCategories)];
	}
	
	__syncthreads();
	
	if(categoryIdx == 0 && siteIdx < numSites){
		for(int cat = 1; cat < numCategories; cat++){
			sharedDoubleBuffer[siteBlockIdx] += sharedDoubleBuffer[cat * blockDim.x + siteBlockIdx];
		}
		double siteLikelihoodInv = invSites[siteIdx + split_offset] * pInv;
		ufScaling[siteIdx + split_offset] = log(sharedDoubleBuffer[siteBlockIdx] + siteLikelihoodInv) * weights[siteIdx + split_offset];
	}	
}

extern "C"
__global__ void reduceSites(double* g_odata, double* g_idata, int n){
	
	__shared__ double temp[2115];
	int thid = threadIdx.x;
	int offset = 1;
	
	int ai = thid;  
	int bi = thid + (MAX_ELEMENTS_PER_BLOCK/2);  
	int bankOffsetA = CONFLICT_FREE_OFFSET(ai);  
	int bankOffsetB = CONFLICT_FREE_OFFSET(bi);

	//Copy data from global memory to shared memory and apply
	// padding for sizes that are not exponents of 2
	
	int blockOffset = MAX_ELEMENTS_PER_BLOCK * blockIdx.x;
	
	if((blockOffset + ai) < n){
		temp[ai + bankOffsetA] = g_idata[blockOffset + ai];
	}else{
		temp[ai + bankOffsetA] = 0;
	}
	
	if((blockOffset + bi) < n){
		temp[bi + bankOffsetB] = g_idata[blockOffset + bi];
	}else{
		temp[bi + bankOffsetB] = 0;
	}
	
	for(int d = MAX_ELEMENTS_PER_BLOCK >> 1; d > 0; d >>= 1){
		__syncthreads();
		if(thid < d){
			int ai = offset * (2 * thid + 1) - 1;
			int bi = offset * (2 * thid + 2) - 1;
			ai += CONFLICT_FREE_OFFSET(ai);
			bi += CONFLICT_FREE_OFFSET(bi);
			
			temp[bi] += temp[ai];
		}
		offset *= 2;
	}
	
	if(thid == 0){
		g_idata[blockIdx.x] = temp[MAX_ELEMENTS_PER_BLOCK - 1 + CONFLICT_FREE_OFFSET(MAX_ELEMENTS_PER_BLOCK - 1)];
	}	
	__syncthreads();
	if(thid == 0 && blockIdx.x == 0 && gridDim.x == 1) g_odata[0] = g_idata[0];
}