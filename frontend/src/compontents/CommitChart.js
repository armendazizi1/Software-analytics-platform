import React from 'react';
import ReactApexChart from "react-apexcharts";
import SimpleService from '../services/SimpleService'
import CommitDialog from './CommitDialog'

class CommitChart extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      commits: this.props.data,
      tripeCommits:[],
      commit2:{},
      gotData:false,
      projectName: this.props.projectName,
      showModal: false,
      pointId: 0,
      commitId:0,
      series: this.generateCommitData(),
      options: {
        colors : ['#2983FF','#4CAF50','#D7263D' ],
        chart: {
          animations: {
            enabled: false
          },
          height: 350,
          type: 'scatter',
          zoom: {
            type: 'xy'
          },
          events: {
            dataPointSelection: (event, chartContext, config) => {
              // console.log(event,chartContext,config)
              const pointId = config.dataPointIndex;
              const seriesIndex = config.seriesIndex
              // var commit = this.state.commits[pointId];
              var commit = this.state.tripeCommits[seriesIndex].data[pointId];
              this.setState({commitId:commit.id})

              this.handleShow();
              this.handleData(pointId);
              // console.log("Selected commit:", commit)
              
              this.setState({gotData:false})
              this.fetchCommitData(commit.id, this.props.projectName)
            }
          }
        },
        tooltip: {
          x: {
            format: 'dd MMM yyyy'
          },
          y: {
            formatter: (value, { series, seriesIndex, dataPointIndex, w }) => {
              // var commit = this.state.commits[parseInt(dataPointIndex)];
              var commit = this.state.tripeCommits[parseInt(seriesIndex)].data[parseInt(dataPointIndex)];

              return value + ":" + commit.created_at.getMinutes();
            },
            title: {
              formatter: (seriesName, e) => {
                // var commit = this.state.commits[parseInt(e.dataPointIndex)];
                // console.log(this.state.tripeCommits)
                // console.log('seriesiNDEX', e.seriesIndex)
                var commit = this.state.tripeCommits[e.seriesIndex].data[parseInt(e.dataPointIndex)];
                // console.log("this is commit",commit)
                return "commit-id: " + commit.id;
              },
            },
          },
        },
        dataLabels: {
          enabled: false
        },
        grid: {
          xaxis: {
            lines: {
              show: true
            }
          },
          yaxis: {
            lines: {
              show: true
            }
          },
        },
        xaxis: {
          type: 'datetime',
          labels: {
            format: 'dd MMM yyyy',
          }
        },
        yaxis: {
          // max: 24,
          title: {
            text: "Time",
            style: {
              fontSize: '16px',
            },
          }
        }
      },


    };

    this.handleShow = this.handleShow.bind(this);
    this.handleData = this.handleData.bind(this);
    this.handleGotData = this.handleGotData.bind(this);
    this.getNextCommit = this.getNextCommit.bind(this);
    this.generateCommitData = this.generateCommitData.bind(this);
  }


  componentDidMount() {
    var InducingBugs = {
      name: 'Inducing Bug',
      data:[]
    }

    var FixingBug = {
      name: 'Fixing Bug',
      data:[]
    }

    var NormalCommit = {
      name: 'Normal',
      data:[]
    }

    var series = [];
    this.props.data.forEach((i) => {
      // var x = i.created_at.getTime();
      // var y = i.created_at.getHours();
      
      if (i.state.includes('resolvesBug') && !i.state.includes('inducingBug')){
        FixingBug.data.push(i);
      }
      else if(i.state.includes('inducingBug')){
        InducingBugs.data.push(i);
      }
      else{
        NormalCommit.data.push(i);
      }

    })


    
    series.push(NormalCommit)
    series.push(FixingBug)
    series.push(InducingBugs)

    this.setState({tripeCommits:series})

  }

  handleShow() {
    if(this.state.showModal === false){
      this.setState({ gotData: false })
    }
    this.setState({ showModal: !this.state.showModal })
  }

  getNextCommit(e){
    // console.log('eeeeeeee',e)
    this.setState({commitId:e})
    this.fetchCommitData(e,this.state.projectName)
  }

  handleGotData(){
    this.setState({ gotData: false })
  }

  handleData(pointId) {
    this.setState({ pointId })

  }

  fetchCommitData(commitId, projectName) {
    this.setState({gotData:false});
    var commit2={};
    SimpleService.getCommitData(commitId,projectName).then((response)=>{
      console.log("Response from commitData",response.data)
      var commitData2 = response.data.left;
      var commitDataMetrics = response.data.right.left;
      var commitDataMetricsComparison = response.data.right.right;

      commit2.cbo = {
        metric:commitDataMetrics.couplingBetweenObjects.toFixed(2),
        comparison: commitDataMetricsComparison.couplingBetweenObjects.toFixed(2)
      };
      commit2.lcom = {
        metric:commitDataMetrics.lackCohesionMethods.toFixed(2),
        comparison: commitDataMetricsComparison.lackCohesionMethods.toFixed(2),
      };
      commit2.loc = {
        metric:commitDataMetrics.linesOfCode.toFixed(2),
        comparison: commitDataMetricsComparison.linesOfCode.toFixed(2)
      };
      commit2.wmc = {
        metric:commitDataMetrics.weightMethodClass.toFixed(2),
        comparison: commitDataMetricsComparison.weightMethodClass.toFixed(2)
      };

      commit2.id = commitData2.id;
      commit2.created_at = new Date(commitData2.date)
      commit2.author = commitData2.author;
      commit2.body = commitData2.body;
      commit2.files_added = commitData2.difference.added ? commitData2.difference.added : [];
      commit2.files_removed = commitData2.difference.deleted  ? commitData2.difference.deleted  : [];
      commit2.files_modified = commitData2.difference.modified  ? commitData2.difference.modified  : [];
      commit2.files_renamed = commitData2.difference.renamed  ? commitData2.difference.renamed  : [];
      commit2.note = commitData2.note;
      commit2.message = commitData2.message;
      commit2.next = commitData2.next;
      let label = '';
      if(commitData2.labels){
        let labels = commitData2.labels
        if (labels.includes('resolvesBug') && !labels.includes('inducingBug')){
          label = 'resolvesBug'
        }
        else if(labels.includes('inducingBug')){
          label = 'inducingBug'
        }

      }

      commit2.label = label
      commit2.resolvingBugIntroducedBy = commitData2.resolvingBugIntroducedBy? commitData2.resolvingBugIntroducedBy:[];
      commit2.inducingBugFixedBy = commitData2.inducingBugFixedBy? commitData2.inducingBugFixedBy:[];

      commit2.clossesIssue = commitData2.issueNumber?commitData2.issueNumber:null;
      // console.log("this is commit2",commit2)
      this.setState({commit2, gotData:true})
    })

  }

  generateCommitData() {

    var InducingBugs = {
      name: 'Inducing Bug',
      data:[]
    }

    var FixingBug = {
      name: 'Fixing Bug',
      data:[]
    }

    var NormalCommit = {
      name: 'Normal',
      data:[]
    }

    var series = [];
    this.props.data.forEach((i) => {
      var x = i.created_at.getTime();
      var y = i.created_at.getHours();
      if (i.state.includes('resolvesBug') && !i.state.includes('inducingBug')){
        FixingBug.data.push([x, y]);
      }
      else if(i.state.includes('inducingBug')){
        InducingBugs.data.push([x, y]);
      }
      else{
        NormalCommit.data.push([x, y]);
      }

    })


    
    series.push(NormalCommit)
    series.push(FixingBug)
    series.push(InducingBugs)

    // this.setState({tripeCommits:series})


    return series;
  }



  render() {
    // console.log("this is commitId in render ", this.state.commitId)
    return (

      <div>
        <div id="chart">
          <ReactApexChart options={this.state.options} series={this.state.series} type="scatter" height={500} />
        </div>
        {(this.state.commits.length > 0  && this.state.commitId !== 0)&&
        <CommitDialog getNextCommit={this.getNextCommit} commitId={this.state.commitId} projectName={this.state.projectName} gotData={this.state.gotData} commitData={this.state.commit2} data={this.state.commits} show={this.state.showModal} handleData={this.handleData} handleShow={this.handleShow} pointId={this.state.pointId} />}
      </div>


    );
  }
}

export default CommitChart      