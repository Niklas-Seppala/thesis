BINARY=wordindex
OUT=out/
CODEDIRS=. src
JVM_HEADERS= $(JAVA)/include $(JAVA)/include/linux
INCDIRS=./include/ $(JVM_HEADERS)
DEPFLAGS=-MP -MD
CFILES=$(foreach D,$(CODEDIRS),$(wildcard $(D)/*.c))
OBJECTS=$(patsubst %.c,%.o,$(CFILES))
DEPFILES=$(patsubst %.c,%.d,$(CFILES))
-include $(DEPFILES)

############## DEBUG BUILDS ###############
dbg_compile: UBSAN=-fsanitize=undefined
dbg_compile: ASAN=-fsanitize=address -static-libasan
dbg_compile: DEBUG=-g
dbg_compile: OPT=-O0
dbg_compile: RT_NULL_CHECKS=-DNULL_CHECKS -DNULL_KILLS
###########################################

CC=gcc
CC_WARN=-Wall -Wshadow -Wextra -Wformat=2 -Wpedantic -fmax-errors=10 -Wno-unknown-pragmas
OPT=-O3
CFLAGS=${CC_WARN} $(OPT) -std=gnu11 ${DEBUG} -DVEC_DEF_CAP=8 ${RT_NULL_CHECKS} $(foreach D,$(INCDIRS),-I$(D)) ${DEPFLAGS}

.PHONY: all clean dbg_compile test mkdirs

all: mkdirs $(OUT)$(BINARY).so

##############################################
#   Create shared library from object files
##############################################
$(OUT)$(BINARY).so: $(OBJECTS)
	$(CC) -shared -fPIC -o $@ $^

##############################################
#   Create object files from src dir
##############################################
%.o:%.c
	$(CC) $(CFLAGS) -c -fPIC -o $@ $< $(ASAN) $(UBSAN)

##############################################
#    Create test binary
##############################################
$(OUT)$(BINARY).test: $(OBJECTS) test/test.o
	$(CC) -o $@ $^ $(ASAN) $(UBSAN)


dbg_compile: $(OUT)$(BINARY).test


##############################################
#   Compile test src
##############################################

test: dbg_compile
	@./$(OUT)${BINARY}.test ${RUN_ARGS}

clean:
	@rm -rf $(OUT)/* $(OUT)$(BINARY).test $(OBJECTS) test/test.o $(DEPFILES) 2>/dev/null || true

mkdirs:
	@mkdir -p $(OUT)