# OCL Highlight Plugin
## (USE plugin for OCL syntax highlight on UML diagrams)
This project was developed within the [Software Systems Engineering group](https://ciencia.iscte-iul.pt/centres/istar-iul/groups/sse) at the [ISTAR Research Center](https://ciencia.iscte-iul.pt/centres/istar-iul) at the [ISCTE-IUL university](https://www.iscte-iul.pt/) in Lisbon, Portugal.

## Introduction
The Unified Modeling Language (UML) [1] was created by the Object Management Group (OMG) [2], and had its first specification draft proposed in January 1997. It’s currently the standard language used in software development for specifying, visualizing, constructing, and documenting artifacts of software systems. However, UML models aren’t typically precise enough to express all relevant aspects of a specification. To fill the existing gaps on these models, a formal language was proposed. OCL (Object Constraint Language) [3] can be used to express constraints (class invariants, pre- and post- conditions, ...), which allow UML models to be more precise and unambiguous. Over the past years, several studies have been conducted to assess the benefits of using OCL alongside UML models [4, 5], which has been proven advantageous to modelers once they overcome OCL’s initial learning curve.

Several support tools have been developed to assist in model-driven development, including the analysis and design phases where modelers need to interpret and write OCL expressions. These tools have their specific characteristics and provide a variety of useful functionalities including syntactic analysis, connection with the UML model, and debugging [6]. To the best of our knowledge, none of these tools provides syntax highlighting in the UML diagram for manually introduced OCL expressions, which we believe that could soften the learning curve for this language by reducing the mental burden when reading, analyzing and writing expressions.

The OCL Highlight Plugin, which was developed in Java, provides a new OCL evaluation dialog that offers syntax highlighting in the
UML model when users evaluate a given OCL expression. The last version of this plugin also includes a ‘Config’ button, where the user can configure different colors for the highlight, and an action button to evaluate the complexity of OCL expressions (using metrics defined by Reynoso et al [7]).

## Notes (version 1.2)
### Installation
Put the OclHighlight-1.2.jar file in your use-x.x.x/plugins folder.

Run USE. A button with a red marker will appear on the plugins section.

### Requirements
Make sure you have USE installed.

## How to use


## Bibliography
* [1] Object Management Group. What is UML | Unified Modeling Language, 2005. Available: http://www.uml.org/what-is-uml.html.
* [2] Object Management Group (OMG). OMG - Object Management
Group, 2017. Available: http://www.omg.org/.
* [3] Object Constraint Language, version 2.4. Tech. Rep. February, 2014.
* [4] Briand, L. C., Labiche, Y., Di Penta, M., and Yan-Bondoc, H. An experimental investigation of formality in UML-based development. IEEE Transactions on Software Engineering (2005).
* [5] Briand, L. C., Labiche, Y., Yan, H. D., and Di Penta, M. A controlled experiment on the impact of the object constraint language in UML-based maintenance. In IEEE International Conference on Software Maintenance, ICSM (2004), pp. 380–389.
* [6] Toval, A., Requena, V., and Fernández, J. L. Emerging OCL tools. Software and Systems Modeling 2, 4 (dec 2003), 248–261.
* [7] Reynoso, L., Genero, M., and Piattini, M. Measuring Ocl Expressions: an Approach Based on Cognitive Techniques. In Metrics for Software Conceptual Models. Imperial College Press, Distributed by World Scientific Publishing Co., jan 2005, pp. 161–206.