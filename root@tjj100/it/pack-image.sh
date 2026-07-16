root@ib-192:~/ib# cat pack-images.sh
#!/bin/bash

# Global variables and default values
COMPOSE_FILE="docker-compose.yaml"
OUTPUT_FILE="all_docker_images.tar.gz"
INPUT_LOAD_FILE="" # New variable for load mode input
MODE=""              # Will be set to "package" or "load"

# Function to display usage information
usage() {
  echo "Usage: $0 [MODE_OPTIONS] [GLOBAL_OPTIONS]"
  echo ""
  echo "Modes:"
  echo "  -p                       : Package images from docker-compose.yaml (default mode)"
  echo "  -l                       : Load images from a .tar.gz file"
  echo ""
  echo "Packaging Options (for -p mode):"
  echo "  -f <docker-compose-file> : Specify the path to your docker-compose.yaml file (default: docker-compose.yaml)"
  echo "  -o <output-tar-gz-file>  : Specify the name of the output .tar.gz file (default: all_docker_images.tar.gz)"
  echo ""
  echo "Loading Options (for -l mode):"
  echo "  -i <input-tar-gz-file>   : Specify the .tar.gz file to load images from (REQUIRED for -l mode)"
  echo ""
  echo "Global Options:"
  echo "  -h                       : Display this help message"
  exit 1
}

# --- Functions for Packaging and Loading ---

package_images() {
  echo "--- Docker Image Packaging Script ---"
  echo "Starting process at $(date)"
  echo "Using Docker Compose file: '$COMPOSE_FILE'"
  echo "Output file: '$OUTPUT_FILE'"
  echo "------------------------------------"
  echo ""

  echo "Step 1/4: Extracting image names from '$COMPOSE_FILE'..."
  # Attempt to use 'docker compose config --images' for Compose V2 (recommended)
  # Fallback to grep/awk if the above fails or is not available.
  IMAGE_NAMES=$(docker compose -f "$COMPOSE_FILE" config --images 2>/dev/null)

  if [ -z "$IMAGE_NAMES" ]; then
    echo "  Warning: 'docker compose config --images' failed or found no images. Attempting fallback parsing..."
    IMAGE_NAMES=$(grep "image:" "$COMPOSE_FILE" | awk '{print $2}' | sort -u)
  fi

  if [ -z "$IMAGE_NAMES" ]; then
    echo "Error: No images found in '$COMPOSE_FILE' or unable to parse. Please check your file and Docker Compose version."
    exit 1
  fi

  echo "  Found the following images:"
  for img in $IMAGE_NAMES; do
    echo "    - $img"
  done
  echo ""

#  echo "Step 2/4: Pulling images (if not already local)..."
 # for img in $IMAGE_NAMES; do
 #   echo "  Pulling '$img'..."
 #   docker pull "$img" || { echo "  Warning: Failed to pull '$img'. It might be a locally built image or an issue with network/registry."; }
 # done
 # echo ""

  echo "Step 3/4: Preparing output directory..."
  # Ensure the directory for the output file exists
  OUTPUT_DIR=$(dirname "$OUTPUT_FILE")
  if [ ! -d "$OUTPUT_DIR" ]; then
    echo "  Creating output directory: '$OUTPUT_DIR'"
    mkdir -p "$OUTPUT_DIR"
    if [ $? -ne 0 ]; then
      echo "Error: Could not create output directory '$OUTPUT_DIR'."
      exit 1
    fi
  else
    echo "  Output directory '$OUTPUT_DIR' already exists."
  fi
  echo ""

  echo "Step 4/4: Packaging all images into '$OUTPUT_FILE'..."
  echo "  This might take a while depending on image sizes and number of images."

  # Check if 'pv' command exists for progress
  if command -v pv &> /dev/null; then
      echo "  'pv' (Pipe Viewer) found. Using it to show progress..."
      docker save $IMAGE_NAMES | pv -cN 'Packaging Images' | gzip > "$OUTPUT_FILE"
  else
      echo "  'pv' (Pipe Viewer) not found. No real-time progress will be shown during packaging."
      echo "  To see progress, install 'pv' (e.g., sudo apt-get install pv or brew install pv)."
      docker save $IMAGE_NAMES | gzip > "$OUTPUT_FILE"
  fi

  if [ $? -eq 0 ]; then
    echo ""
    echo "------------------------------------"
    echo "SUCCESS: Images packaged."
    echo "Output file: '$OUTPUT_FILE'"
    echo "File size: $(ls -lh "$OUTPUT_FILE" | awk '{print $5}')"
    echo "Finished process at $(date)"
    echo "------------------------------------"
  else
    echo ""
    echo "------------------------------------"
    echo "ERROR: Failed to package images."
    echo "Please check the error messages above."
    echo "Finished process at $(date)"
    echo "------------------------------------"
    exit 1
  fi
}

load_images() {
  echo "--- Docker Image Loading Script ---"
  echo "Starting process at $(date)"
  echo "Input file: '$INPUT_LOAD_FILE'"
  echo "------------------------------------"
  echo ""

  # Check if input file exists
  if [ ! -f "$INPUT_LOAD_FILE" ]; then
    echo "Error: Input file for loading '$INPUT_LOAD_FILE' not found."
    exit 1
  fi

  echo "Step 1/1: Loading images from '$INPUT_LOAD_FILE'..."
  echo "  This might take a while depending on image sizes."

  # Check if 'pv' command exists for progress
  if command -v pv &> /dev/null; then
      echo "  'pv' (Pipe Viewer) found. Using it to show progress..."
      # Use pv differently based on whether it's a .gz file
      if [[ "$INPUT_LOAD_FILE" == *.gz ]]; then
          gunzip -c "$INPUT_LOAD_FILE" | pv -cN 'Loading Images' | docker load
      else
          pv -cN 'Loading Images' < "$INPUT_LOAD_FILE" | docker load
      fi
  else
      echo "  'pv' (Pipe Viewer) not found. No real-time progress will be shown during loading."
      echo "  To see progress, install 'pv' (e.g., sudo apt-get install pv or brew install pv)."
      if [[ "$INPUT_LOAD_FILE" == *.gz ]]; then
          gunzip -c "$INPUT_LOAD_FILE" | docker load
      else
          docker load < "$INPUT_LOAD_FILE"
      fi
  fi

  if [ $? -eq 0 ]; then
    echo ""
    echo "------------------------------------"
    echo "SUCCESS: Images loaded."
    echo "Finished process at $(date)"
    echo "------------------------------------"
  else
    echo ""
    echo "------------------------------------"
    echo "ERROR: Failed to load images."
    echo "Please check the error messages above."
    echo "Finished process at $(date)"
    echo "------------------------------------"
    exit 1
  fi
}

# --- Main Script Logic ---

# Parse command-line arguments
while getopts "f:o:i:lph" opt; do
  case $opt in
    f) COMPOSE_FILE="$OPTARG"; MODE="package" ;;
    o) OUTPUT_FILE="$OPTARG"; MODE="package" ;;
    i) INPUT_LOAD_FILE="$OPTARG"; MODE="load" ;;
    l) MODE="load" ;; # Explicitly set load mode
    p) MODE="package" ;; # Explicitly set package mode
    h) usage ;;
    \?) echo "Invalid option: -$OPTARG" >&2; usage ;;
  esac
done
shift $((OPTIND-1))

# Default mode if none specified
if [ -z "$MODE" ]; then
    MODE="package"
fi

# Validate options based on the chosen mode
if [ "$MODE" == "package" ]; then
    if [ -n "$INPUT_LOAD_FILE" ] && [ "$INPUT_LOAD_FILE" != "$OUTPUT_FILE" ]; then
        echo "Error: Cannot specify an input file (-i) when in packaging mode."
        usage
    fi
    # Check if the compose file exists for packaging
    if [ ! -f "$COMPOSE_FILE" ]; then
      echo "Error: Docker Compose file '$COMPOSE_FILE' not found for packaging."
      usage
    fi
elif [ "$MODE" == "load" ]; then
    if [ -n "$COMPOSE_FILE" ] && [ "$COMPOSE_FILE" != "docker-compose.yaml" ]; then
        echo "Error: Cannot specify a compose file (-f) when in loading mode."
        usage
    fi
    if [ -n "$OUTPUT_FILE" ] && [ "$OUTPUT_FILE" != "all_docker_images.tar.gz" ]; then
        echo "Error: Cannot specify an output file (-o) when in loading mode."
        usage
    fi
    # Ensure -i is provided for load mode
    if [ -z "$INPUT_LOAD_FILE" ]; then
        echo "Error: An input .tar.gz file must be specified with -i when in loading mode."
        usage
    fi
    # Check if the input file exists for loading
    if [ ! -f "$INPUT_LOAD_FILE" ]; then
      echo "Error: Input file for loading '$INPUT_LOAD_FILE' not found."
      usage
    fi
fi

# Execute the chosen mode
if [ "$MODE" == "package" ]; then
    package_images
elif [ "$MODE" == "load" ]; then
    load_images
else
    echo "Internal script error: Invalid mode '$MODE'."
    exit 1
fi
